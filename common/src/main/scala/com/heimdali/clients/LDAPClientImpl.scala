package com.heimdali.clients

import cats.data.OptionT
import cats.effect.{Effect, Sync}
import cats.implicits._
import com.heimdali.config._
import com.heimdali.services.{MemberSearchResult, MemberSearchResultItem}
import com.typesafe.scalalogging.LazyLogging
import com.unboundid.ldap.sdk._
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}
import org.fusesource.scalate.{Template, TemplateEngine}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class LDAPClientImpl[F[_]: Effect](ldapConfig: LDAPConfig, binding: LDAPConfig => LDAPBinding)
    extends LDAPClient[F] with LazyLogging {

  val templateEngine: TemplateEngine = new TemplateEngine()
  val filterTemplate: Template = templateEngine.compileText("mustache", ldapConfig.filterTemplate)
  val displayTemplate: Template = templateEngine.compileText("mustache", ldapConfig.memberDisplayTemplate)

  val ldapBinding: LDAPBinding = binding(ldapConfig)

  val connectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory

    val servers: Array[String] = ldapBinding.server.split(",")
    val ports: Array[Int] = Array.fill(servers.length)(ldapBinding.port)

    val failoverSet = new FailoverServerSet(servers, ports, sslSocketFactory)

    val bindRequest: SimpleBindRequest =
      new SimpleBindRequest(ldapBinding.bindDN, ldapBinding.bindPassword)

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
      searchResultEntry.getDN,
      Option(searchResultEntry.getAttributeValues("memberOf")).map(_.toSeq).getOrElse(Seq.empty),
      Option(searchResultEntry.getAttributeValue("mail"))
    )

  def groupObjectClass: String =
    "group"

  def getUserEntry(username: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay {
      val searchResult =
        connectionPool.search(ldapConfig.baseDN, SearchScope.SUB, searchQuery(username))
      searchResult.getSearchEntries.asScala.headOption
    })

  def getEntry(dn: String): OptionT[F, SearchResultEntry] =
    OptionT(
      Sync[F].delay(
        try {
          logger.debug(s"getting info for $dn")
          Option(connectionPool.getEntry(dn))
        } catch {
          case exc: Throwable =>
            logger.info(s"Failed to get LDAP entry for dn '$dn'", exc)
            None
        }
      )
    )

  override def findUser(distinguishedName: String): OptionT[F, LDAPUser] =
    getEntry(distinguishedName).map(ldapUser)

  override def validateUser(username: String, password: String): OptionT[F, LDAPUser] =
    for {
      result <- getUserEntry(username).map(ldapUser) // TODO switch order so search happens after validate
      _ <- OptionT(Sync[F].delay(ldapBindingAsOption(result.distinguishedName, password, username)))
    } yield result

  override def getUser(username: String): OptionT[F, LDAPUser] = getUserEntry(username).map(ldapUser)

  private def ldapBindingAsOption(distinguishedName: String, password: String, userName: String): Option[BindResult] = {
    Try(connectionPool.bind(distinguishedName, password)) match {
      case Success(value) => Some(value)
      case Failure(exception) => {
        logger.warn(s"Bind process failed for user $userName", exception)
        None
      }
    }
  }

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

  def requestGroup(groupDN: String, groupName: String, attributes: List[(String, String)]): F[Unit] =
    groupRequest(groupDN, groupName, attributes).map {
      case Some(request: AddRequest) =>
        logger.info("adding group with {}", request)
        connectionPool.add(request)
      case Some(request: ModifyRequest) =>
        logger.info("updating group with {}", request)
        connectionPool.modify(request)
      case None =>
        logger.info("existing group had no changes")
        ()
    }

  override def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit] =
    for {
      _ <- Sync[F].pure(logger.info("creating group {}", groupName))
      groupDN = attributes.find(_._1 == "dn").get._2
      rest = attributes.filterNot(_._1 == "dn")
      _ <- requestGroup(groupDN, groupName, rest)
      _ <- Sync[F].pure(logger.info("group {} created", groupName))
    } yield ()

  def createMemberAttribute(groupEntry: SearchResultEntry, newMember: String): F[Option[Unit]] =
    if (!groupEntry.hasAttribute("member") || !groupEntry.getAttributeValues("member").contains(newMember))
      for {
        res <- Effect[F].delay(connectionPool.modify(
              groupEntry.getDN, new Modification(ModificationType.ADD, "member", newMember)))
      } yield {
        if (res.getResultCode.intValue() == 0) { Some(()) }
        else {
          logger.error("Adding member failed ", res.getDiagnosticMessage)
          None
        }
      }

    else Option.empty[Unit].pure[F]

  override def addUser(groupDN: String, distinguishedName: String): OptionT[F, String] =
    for {
      _ <- OptionT.liftF(Sync[F].pure(logger.info("getting group {}", groupDN)))
      groupEntry <- getEntry(groupDN)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("found group {}", groupEntry)))
      _ <- OptionT.liftF(createMemberAttribute(groupEntry, distinguishedName))
      _ <- OptionT.liftF(Sync[F].pure(logger.info("added {} to {}", distinguishedName, groupDN)))
    } yield distinguishedName

  override def groupMembers(groupDN: String): F[List[LDAPUser]] =
    Sync[F].delay {
      val searchResult =
        connectionPool.search(
          ldapConfig.baseDN,
          SearchScope.SUB,
          s"(&(objectClass=user)(memberOf=$groupDN))"
        )
      searchResult.getSearchEntries.asScala.map(ldapUser).toList
    }

  override def removeUser(groupDN: String, memberDN: String): OptionT[F, String] =
    OptionT(getEntry(groupDN).value.map {
      case Some(group) if group.hasAttribute("member") && group.getAttributeValues("member").contains(memberDN) =>
        connectionPool.modify(groupDN, new Modification(ModificationType.DELETE, "member", memberDN))
        Some(memberDN)
      case _ => Some(memberDN) //no-op
    })

  def lookup(filter: String): F[List[SearchResultEntry]] =
    Effect[F].delay {
      val filterText = templateEngine.layout(filterTemplate.source, Map("filter" -> filter))
      logger.debug("looking up users with \"{}\"", filterText)
      connectionPool
        .search(ldapConfig.baseDN, SearchScope.SUB, filterText)
        .getSearchEntries
        .asScala
        .toList
    }

  def genDisplay(searchResultEntry: SearchResultEntry): String = {
    val attributes = searchResultEntry.getAttributes.asScala.map(e => e.getName -> e.getValue).toMap
    templateEngine.layout(displayTemplate.source, attributes)
  }

  def generateSearchResult(searchResultEntry: SearchResultEntry): MemberSearchResultItem =
    MemberSearchResultItem(genDisplay(searchResultEntry), searchResultEntry.getDN)

  override def search(filter: String): F[MemberSearchResult] =
    lookup(filter).map { results =>
      MemberSearchResult(
        results.filter(_.getObjectClassValues.exists(_ == "user")).map(generateSearchResult),
        results.filter(_.getObjectClassValues.exists(_ == "group")).map(generateSearchResult)
      )
    }

  override def deleteGroup(groupDN: String): OptionT[F, String] =
    for {
      _ <- getEntry(groupDN)
      _ <- OptionT(Option(connectionPool.delete(groupDN)).pure[F])
    } yield groupDN
}
