package com.heimdali.clients

import cats.data.{EitherT, OptionT}
import cats.effect.{Effect, Sync}
import cats.implicits._
import com.heimdali.config._
import com.typesafe.scalalogging.LazyLogging
import com.unboundid.ldap.sdk._
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

  def createGroup(groupName: String, attributes: List[(String, String)]): EitherT[F, GroupCreationError, Unit]

  def addUser(groupName: String, distinguishedName: String): OptionT[F, String]

  def removeUser(groupName: String, distinguishedName: String): OptionT[F, String]

  def groupMembers(groupDN: String): F[List[LDAPUser]]

  def search(filter: String): F[List[SearchResultEntry]]
}

abstract class LDAPClientImpl[F[_] : Effect](
                                              val ldapConfig: LDAPConfig,
                                              val connectionFactory: () => LDAPConnection
                                            ) extends LDAPClient[F]
  with LazyLogging {

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

  def requestGroup(groupName: String, attributes: List[(String, String)]): EitherT[F, GroupCreationError, LDAPResult] =
    EitherT(Sync[F].delay {
      try {
        Right(connectionFactory().add(attributes.toMap.get("dn").get, attributes.filterNot(_._1 == "dn").map(attribute): _*))
      } catch {
        case exc: LDAPException if exc.getResultCode == ResultCode.ENTRY_ALREADY_EXISTS =>
          logger.warn("group {} already exists", groupName)
          Left(GroupAlreadyExists)
        case exc: Throwable =>
          logger.error("couldn't create ldap group")
          logger.error(exc.getMessage, exc)
          Left(GeneralError(exc))
      }
    })

  override def createGroup(groupName: String, attributes: List[(String, String)]): EitherT[F, GroupCreationError, Unit] =
    for {
      _ <- EitherT.liftF(Sync[F].pure(logger.info("creating group {}", groupName)))
      _ <- requestGroup(groupName, attributes)
      _ <- EitherT.liftF(Sync[F].pure(logger.info("group {} created", groupName)))
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
