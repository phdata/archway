package io.phdata.clients

import cats.data.OptionT
import cats.effect.{Effect, Sync}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import com.unboundid.ldap.sdk._
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager, TrustStoreTrustManager}
import io.phdata.config._
import io.phdata.models.DistinguishedName
import io.phdata.services.{MemberSearchResult, MemberSearchResultItem}
import org.fusesource.scalate.{Template, TemplateEngine}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class LDAPClientImpl[F[_]: Effect](ldapConfig: LDAPConfig, binding: LDAPConfig => LDAPBinding)
    extends LDAPClient[F] with LazyLogging {

  // Enable unbound debug logging at application debug level. https://docs.ldap.com/ldap-sdk/docs/getting-started/debug.html
  private val UNBOUND_DEBUG_LOGGING_PROPERTY = "com.unboundid.ldap.sdk.debug.enabled"
  if (logger.underlying.isDebugEnabled) {
    logger.debug(s"Enabling unbound logging $UNBOUND_DEBUG_LOGGING_PROPERTY")
    System.setProperty(UNBOUND_DEBUG_LOGGING_PROPERTY, "true")
  }

  val templateEngine: TemplateEngine = new TemplateEngine()
  val filterTemplate: Template = templateEngine.compileText("mustache", ldapConfig.filterTemplate)
  val displayTemplate: Template = templateEngine.compileText("mustache", ldapConfig.memberDisplayTemplate)

  val ldapBinding: LDAPBinding = binding(ldapConfig)

  val connectionPool: LDAPConnectionPool = {
    val sslUtil = if(ldapConfig.ignoreSslCert.getOrElse(false)) {
      new SSLUtil(new TrustAllTrustManager)
    } else {
      new SSLUtil(new TrustStoreTrustManager(System.getProperty("javax.net.ssl.trustStore")))
    }

    val sslSocketFactory = sslUtil.createSSLSocketFactory

    val servers: Array[String] = ldapBinding.server.split(",")
    val ports: Array[Int] = Array.fill(servers.length)(ldapBinding.port)

    val failoverSet = new FailoverServerSet(servers, ports, sslSocketFactory)

    val bindRequest: SimpleBindRequest =
      new SimpleBindRequest(ldapBinding.bindDN, ldapBinding.bindPassword.value)

    Try(new LDAPConnectionPool(failoverSet, bindRequest, 4)) match {
      case Success(value) => value
      case Failure(exception) => {
        logger.error(s"Creation of LDAPConnectionPool failed ${exception.getLocalizedMessage}", exception)
        throw exception
      }
    }
  }

  def searchQuery(username: String): String =
    s"(sAMAccountName=$username)"

  def fullUsername(username: String): String =
    s"$username@${ldapConfig.realm}"

  def ldapUser(searchResultEntry: SearchResultEntry) =
    LDAPUser(
      genDisplay(searchResultEntry),
      searchResultEntry.getAttributeValue("sAMAccountName"),
      DistinguishedName(searchResultEntry.getDN),
      Option(searchResultEntry.getAttributeValues("memberOf")).map(_.toSeq).getOrElse(Seq.empty),
      Option(searchResultEntry.getAttributeValue("mail"))
    )

  def getUserEntry(username: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay {
      logger.debug(s"Getting user entry for $username")
      val searchResult =
        connectionPool.search(ldapConfig.baseDN, SearchScope.SUB, searchQuery(username))
      searchResult.getSearchEntries.asScala.headOption
    })

  override def findUserByDN(distinguishedName: DistinguishedName): OptionT[F, LDAPUser] =
    getEntry(distinguishedName).map(ldapUser)

  override def validateUser(username: String, password: Password): OptionT[F, LDAPUser] =
    getUserEntry(username).map(ldapUser).flatMap { result =>
      if (ldapConfig.authorizationDN.isEmpty) {
        for {
          _ <- OptionT(Sync[F].delay(ldapBindingAsOption(result.distinguishedName.value, password, username)))
        } yield result
      } else {
        for {
          groupUsers <- OptionT.liftF(groupMembers(DistinguishedName(ldapConfig.authorizationDN)))
          _ <- if (groupUsers.map(_.username).contains(username)) {
            OptionT(Sync[F].delay(ldapBindingAsOption(result.distinguishedName.value, password, username)))
          } else {
            OptionT.none[F, BindResult]
          }
        } yield result
      }
    }

  override def findUserByUserName(username: String): OptionT[F, LDAPUser] = getUserEntry(username).map(ldapUser)

  def attributeConvert(attributes: List[(String, String)]): List[Attribute] =
    attributes
      .groupBy(_._1)
      .map {
        case (key, values) => new Attribute(key, values.map(_._2): _*)
      }
      .toList

  // intentionally ignore deletes (especially due to generated attributes)
  def modificationsFor(existing: List[(String, String)], updated: List[(String, String)]): List[Modification] =
    (updated diff existing).map {
      case (key, value) if existing.exists(a => a._1 == key) =>
        logger.debug(
          "changing existing value for {} with a value of {} to {}",
          key,
          existing.find(_._1 == key).get._2,
          value
        )
        new Modification(ModificationType.REPLACE, key, value)
      case (key, value) =>
        logger.debug("adding value for {} with {}", key, value)
        new Modification(ModificationType.ADD, key, value)
    }

  def groupRequest(
      groupDN: String,
      groupName: String,
      attributes: List[(String, String)]
  ): F[Option[_ <: LDAPRequest]] =
    Sync[F].delay {
      logger.debug(s"Getting entry for $groupDN")
      Option(connectionPool.getEntry(groupDN)) match {
        case Some(entry) =>
          val existing = entry.getAttributes.asScala.map(a => a.getName -> a.getValue).toList
          val modifications = modificationsFor(existing, attributes).filterNot(_.getAttributeName == "objectClass")
          if (modifications.isEmpty)
            None
          else
            Some(new ModifyRequest(groupDN, modifications: _*))
        case None =>
          Some(new AddRequest(groupDN, attributeConvert(attributes.filterNot(_._1 == "dn")): _*))
      }
    }

  def requestGroup(groupDN: DistinguishedName, groupName: String, attributes: List[(String, String)]): F[Unit] =
    groupRequest(groupDN.value, groupName, attributes).map {
      case Some(request: AddRequest) =>
        logger.info("adding group with {}", request)
        connectionPool.add(request)
      case Some(request: ModifyRequest) =>
        logger.info("updating group with {}", request)
        connectionPool.modify(request)
      case Some(request) =>
        logger.error("unidentified request type {}", request)
      case None =>
        logger.info("existing group had no changes")
        ()
    }

  override def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit] =
    for {
      _ <- logger.info("creating group {}", groupName).pure[F]
      groupDN = attributes.find(_._1 == "dn").get._2
      rest = attributes.filterNot(_._1 == "dn")
      _ <- requestGroup(DistinguishedName(groupDN), groupName, rest)
      _ <- logger.info("group {} created", groupName).pure[F]
    } yield ()

  override def addUserToGroup(groupDN: DistinguishedName, distinguishedName: DistinguishedName): OptionT[F, String] =
    for {
      _ <- OptionT.liftF(Sync[F].pure(logger.info("getting group {}", groupDN)))
      groupEntry <- getEntry(groupDN)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("found group {}", groupEntry)))
      _ <- OptionT.liftF(createMemberAttribute(groupEntry, distinguishedName.value))
      _ <- OptionT.liftF(Sync[F].pure(logger.info("added {} to {}", distinguishedName, groupDN)))
    } yield distinguishedName.value

  override def groupMembers(groupDN: DistinguishedName): F[List[LDAPUser]] =
    Sync[F].delay {
      val searchResult =
        connectionPool.search(
          ldapConfig.baseDN,
          SearchScope.SUB,
          s"(&(objectClass=user)(memberOf=$groupDN))"
        )
      searchResult.getSearchEntries.asScala.map(ldapUser).toList
    }

  override def removeUserFromGroup(groupDN: DistinguishedName, memberDN: DistinguishedName): OptionT[F, String] =
    OptionT(getEntry(groupDN).value.map {
      case Some(group) if group.hasAttribute("member") && group.getAttributeValues("member").contains(memberDN.value) =>
        logger.info(s"Removing member: $memberDN from group $groupDN")
        connectionPool.modify(groupDN.value, new Modification(ModificationType.DELETE, "member", memberDN.value))
        Some(memberDN.value)
      case _ => {
        logger.info("Removing member: No action is needed")
        Some(memberDN.value)
      } //no-op
    })

  override def search(filter: String): F[MemberSearchResult] =
    lookup(filter).map { results =>
      MemberSearchResult(
        results.filter(_.getObjectClassValues.exists(_ == "user")).map(generateSearchResult),
        results.filter(_.getObjectClassValues.exists(_ == "group")).map(generateSearchResult)
      )
    }

  override def deleteGroup(groupDN: DistinguishedName): OptionT[F, String] =
    for {
      _ <- getEntry(groupDN)
      _ <- OptionT(Option(connectionPool.delete(groupDN.value)).pure[F])
      _ <- OptionT(logger.info(s"Deleting LDAP group $groupDN").some.pure[F])
    } yield groupDN.value

  private def getEntry(dn: DistinguishedName): OptionT[F, SearchResultEntry] = {
    val result: Option[SearchResultEntry] = Try(connectionPool.getEntry(dn.value)) match {
      case Success(value) =>
        logger.debug(s"Got info for $dn")
        Some(value)
      case Failure(e) =>
        logger.info(s"Failed to get LDAP entry for dn '$dn'", e)
        None
    }

    OptionT.fromOption[F](result)
  }

  private def ldapBindingAsOption(
      distinguishedName: String,
      password: Password,
      userName: String
  ): Option[BindResult] = {
    Try(connectionPool.bind(distinguishedName, password.value)) match {
      case Success(value) => Some(value)
      case Failure(exception) => {
        logger.warn(s"Bind process failed for user $userName", exception)
        None
      }
    }
  }

  private def lookup(filter: String): F[List[SearchResultEntry]] =
    Effect[F].delay {
      val filterText = templateEngine.layout(filterTemplate.source, Map("filter" -> filter))
      logger.debug(s"""looking up users with $filterText""")
      connectionPool.search(ldapConfig.baseDN, SearchScope.SUB, filterText).getSearchEntries.asScala.toList
    }

  private def genDisplay(searchResultEntry: SearchResultEntry): String = {
    val attributes = searchResultEntry.getAttributes.asScala.map(e => e.getName -> e.getValue).toMap
    templateEngine.layout(displayTemplate.source, attributes)
  }

  private def generateSearchResult(searchResultEntry: SearchResultEntry): MemberSearchResultItem =
    MemberSearchResultItem(genDisplay(searchResultEntry), searchResultEntry.getDN)

  private def createMemberAttribute(groupEntry: SearchResultEntry, newMember: String): F[Option[Unit]] =
    if (!groupEntry.hasAttribute("member") || !groupEntry.getAttributeValues("member").contains(newMember))
      for {
        res <- Effect[F].delay(
          connectionPool.modify(groupEntry.getDN, new Modification(ModificationType.ADD, "member", newMember))
        )
      } yield {
        if (res.getResultCode.intValue() == 0) {
          logger.debug(s"Member $newMember was successfully added")
          Some(())
        } else {
          logger.error("Adding member failed ", res.getDiagnosticMessage)
          None
        }
      } else Option.empty[Unit].pure[F]
}
