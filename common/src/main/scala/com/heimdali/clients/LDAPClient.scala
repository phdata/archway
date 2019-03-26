package com.heimdali.clients

import cats.data.{EitherT, OptionT}
import cats.effect.{Effect, Sync}
import cats.implicits._
import com.heimdali.config._
import com.typesafe.scalalogging.LazyLogging
import com.unboundid.ldap.sdk._
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}
import org.slf4j.MarkerFactory

import scala.collection.JavaConverters._
import scala.util.Try

sealed trait GroupCreationError

case object GroupAlreadyExists extends GroupCreationError

case class GeneralError(throwable: Throwable) extends GroupCreationError

case class LDAPUser(name: String, username: String, distinguishedName: String, memberships: Seq[String], email: Option[String])

trait LDAPClient[F[_]] {
  def findUser(distinguishedName: String): OptionT[F, LDAPUser]

  def validateUser(username: String, password: String): OptionT[F, LDAPUser]

  def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit]

  def addUser(groupName: String, distinguishedName: String): OptionT[F, String]

  def removeUser(groupName: String, distinguishedName: String): OptionT[F, String]

  def groupMembers(groupDN: String): F[List[LDAPUser]]

  def search(filter: String): F[List[SearchResultEntry]]
}

abstract class LDAPClientImpl[F[_] : Effect](val ldapConfig: LDAPConfig)
  extends LDAPClient[F]
    with LazyLogging {


  val ldapConnectionPool: LDAPConnectionPool = {
    val sslUtil = new SSLUtil(new TrustAllTrustManager)
    val sslSocketFactory = sslUtil.createSSLSocketFactory
    val connection = new LDAPConnection(
      sslSocketFactory,
      ldapConfig.server,
      ldapConfig.port,
      ldapConfig.bindDN,
      ldapConfig.bindPassword
    )
    new LDAPConnectionPool(connection, 10)
  }

  def connectionFactory(): LDAPConnection =
    ldapConnectionPool.getConnection

  def searchQuery(username: String): String

  def fullUsername(username: String): String

  def ldapUser(searchResultEntry: SearchResultEntry): LDAPUser

  def groupObjectClass: String

  def getUserEntry(username: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay {
      val searchResult =
        connectionFactory()
          .search(ldapConfig.baseDN, SearchScope.SUB, searchQuery(username))
      searchResult.getSearchEntries.asScala.headOption
    })

  def getEntry(dn: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay(
      try {
        logger.debug(s"getting info for $dn")
        Option(connectionFactory().getEntry(dn))
      } catch {
        case exc: Throwable =>
          exc.printStackTrace()
          None
      }
    ))

  override def findUser(distinguishedName: String): OptionT[F, LDAPUser] =
    getEntry(distinguishedName).map(ldapUser)

  override def validateUser(
                             username: String,
                             password: String
                           ): OptionT[F, LDAPUser] =
    for {
      bindResult <- OptionT(Sync[F].delay(Try(connectionFactory().bind(fullUsername(username), password)).toOption))
      result <- if (bindResult.getResultCode == ResultCode.SUCCESS)
        getUserEntry(username).map(ldapUser)
      else OptionT[F, LDAPUser](Sync[F].pure(None))
    } yield result

  val guidNumberDN =
    s"CN=${ldapConfig.domain},CN=ypservers,CN=ypServ30,CN=RpcServices,CN=System,${ldapConfig.baseDN}"

  def nextGuid: F[Long] =
    Sync[F].delay(
      connectionFactory()
        .getEntry(guidNumberDN)
        .getAttribute("msSFU30MaxGidNumber")
        .getValueAsLong
    )

  def updateGuid(guid: Long): F[Unit] =
    Sync[F].delay(
      connectionFactory()
        .modify(
          guidNumberDN,
          new Modification(
            ModificationType.REPLACE,
            "msSFU30MaxGidNumber",
            guid.show
          )
        )
    )

  def attribute(pair: (String, String)): Attribute =
    pair match {
      case (key, value) => new Attribute(key, value)
    }

  // intentionally ignore deletes (especially due to generated attributes)
  def modificationsFor(existing: List[(String, String)], updated: List[(String, String)]): List[Modification] =
    (updated diff existing).map {
      case (key, value) if existing.exists(a => a._1 == key) =>
        new Modification(ModificationType.REPLACE, key, value)
      case (key, value) =>
        new Modification(ModificationType.ADD, key, value)
    }

  def groupRequest(groupDN: String, groupName: String, attributes: List[(String, String)]): EitherT[F, AddRequest, ModifyRequest] =
    EitherT(Sync[F].delay {
      Option(connectionFactory().getEntry(groupDN)) match {
        case Some(entry) =>
          val existing = entry.getAttributes.asScala.map(a => a.getName -> a.getValue).toList
          val modifications = modificationsFor(existing, attributes)
          Right(new ModifyRequest(groupDN, modifications: _*))
        case None =>
          Left(new AddRequest(groupDN, attributes.filterNot(_._1 == "dn").map(attribute): _*))
      }
    })

  def requestGroup(groupDN: String, groupName: String, attributes: List[(String, String)]): F[Unit] =
    groupRequest(groupDN, groupName, attributes).value.map {
      case Left(request) =>
        connectionFactory().add(request)
      case Right(request) =>
        connectionFactory().modify(request)
    }

  override def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit] =
    for {
      _ <- Sync[F].pure(logger.info("creating group {}", groupName))
      groupDN = attributes.find(_._1 == "dn").get._2
      rest = attributes.filterNot(_._1 == "dn")
      _ <- requestGroup(groupDN, groupName, rest)
      _ <- Sync[F].pure(logger.info("group {} created", groupName))
    } yield ()

  def createMemberAttribute(groupEntry: SearchResultEntry, newMember: String): OptionT[F, Unit] =
    if (!groupEntry.hasAttribute("member") || !groupEntry
      .getAttributeValues("member")
      .contains(newMember))
      OptionT.liftF(Effect[F]
        .delay(
          connectionFactory()
            .modify(
              groupEntry.getDN,
              new Modification(ModificationType.ADD, "member", newMember)
            )
        ).void)
    else OptionT.none[F, Unit]

  override def addUser(groupDN: String, distinguishedName: String): OptionT[F, String] =
    for {
      _ <- OptionT.liftF(Sync[F].pure(logger.info("getting group {}", groupDN)))
      groupEntry <- getEntry(groupDN)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("found group {}", groupEntry)))
      _ <- createMemberAttribute(groupEntry, distinguishedName)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("added {} to {}", distinguishedName, groupDN)))
    } yield distinguishedName

  override def groupMembers(groupDN: String): F[List[LDAPUser]] =
    Sync[F].delay {
      val searchResult = connectionFactory().search(
        ldapConfig.baseDN,
        SearchScope.SUB,
        s"(&(objectClass=user)(memberOf=$groupDN))"
      )
      searchResult.getSearchEntries.asScala
        .map(ldapUser)
        .toList
    }

  override def removeUser(groupDN: String, memberDN: String): OptionT[F, String] =
    OptionT(getEntry(groupDN).value.map {
      case Some(group) if group.hasAttribute("member") && group.getAttributeValues("member").contains(memberDN) =>
        connectionFactory().modify(groupDN, new Modification(ModificationType.DELETE, "member", memberDN))
        Some(memberDN)
      case _ => Some(memberDN) //no-op
    })

  override def search(filter: String): F[List[SearchResultEntry]] =
    Effect[F].delay(connectionFactory()
      .search(ldapConfig.baseDN, SearchScope.SUB, s"(&(cn=*$filter*)(|(objectClass=user)(objectClass=group)))")
      .getSearchEntries.asScala.toList)
}
