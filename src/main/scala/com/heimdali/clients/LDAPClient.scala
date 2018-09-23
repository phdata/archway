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

case class LDAPUser(name: String, username: String, memberships: Seq[String], email: Option[String])

trait LDAPClient[F[_]] {
  def findUser(username: String): OptionT[F, LDAPUser]

  def validateUser(username: String, password: String): OptionT[F, LDAPUser]

  def createGroup(
      gid: Long,
      groupName: String,
      groupDN: String
  ): EitherT[F, GroupCreationError, Unit]

  def addUser(groupName: String, username: String): OptionT[F, LDAPUser]

  def removeUser(groupName: String, username: String): OptionT[F, LDAPUser]

  def groupMembers(groupDN: String): F[List[LDAPUser]]
}

abstract class LDAPClientImpl[F[_]: Effect](
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

  def getGroupEntry(dn: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay(Try(connectionFactory().getEntry(dn)).toOption))

  override def findUser(username: String): OptionT[F, LDAPUser] =
    getUserEntry(username).map(ldapUser)

  override def validateUser(
      username: String,
      password: String
  ): OptionT[F, LDAPUser] =
    for {
      bindResult <- OptionT(
        Sync[F].delay(
          Try(
            connectionFactory()
              .bind(fullUsername(username), password)
          ).toOption
        )
      )
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

  def requestGroup(
      groupName: String,
      groupDN: String,
      gid: Long
  ): EitherT[F, GroupCreationError, LDAPResult] =
    EitherT(Sync[F].delay {
      try {
        Right(
          connectionFactory().add(
            s"dn: $groupDN",
            s"objectClass: $groupObjectClass",
            "objectClass: top",
            s"sAMAccountName: $groupName",
            s"cn: $groupName",
            s"msSFU30Name: $groupName",
            s"msSFU30NisDomain: ${ldapConfig.domain}",
            s"gidNumber: $gid"
          )
        )
      } catch {
        case exc: LDAPException
            if exc.getResultCode == ResultCode.ENTRY_ALREADY_EXISTS =>
          logger.warn("group {} already exists", groupName)
          Left(GroupAlreadyExists)
        case exc: Throwable =>
          logger.error("couldn't create ldap group")
          logger.error(exc.getMessage, exc)
          Left(GeneralError(exc))
      }
    })

  override def createGroup(
      gid: Long,
      groupName: String,
      groupDN: String
  ): EitherT[F, GroupCreationError, Unit] =
    for {
      _ <- EitherT.liftF(Sync[F].pure(logger.info("creating group {} with guid is {}", groupName, gid)))
      _ <- requestGroup(groupName, groupDN, gid)
      _ <- EitherT.liftF(Sync[F].pure(logger.info("group {} created", groupName)))
    } yield ()

  def createMemberAttribute(
      groupEntry: SearchResultEntry,
      userEntry: SearchResultEntry
  ): OptionT[F, Unit] =
    if (!groupEntry.hasAttribute("member") || !groupEntry
          .getAttributeValues("member")
          .contains(userEntry.getDN))
      OptionT.liftF(Effect[F]
        .delay(
          connectionFactory()
            .modify(
              groupEntry.getDN,
              new Modification(ModificationType.ADD, "member", userEntry.getDN)
            )
        ).void)
    else OptionT.none[F, Unit]

  override def addUser(
      groupDN: String,
      username: String
  ): OptionT[F, LDAPUser] =
    for {
      _ <- OptionT.liftF(Sync[F].pure(logger.info("getting info for {}", username)))
      userEntry <- getUserEntry(username)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("found user {}", userEntry)))
      _ <- OptionT.liftF(Sync[F].pure(logger.info("getting group {}", groupDN)))
      groupEntry <- getGroupEntry(groupDN)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("found group {}", groupEntry)))
      _ <- createMemberAttribute(groupEntry, userEntry)
      _ <- OptionT.liftF(Sync[F].pure(logger.info("added {} to {}", username, groupDN)))
    } yield ldapUser(userEntry)

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

  override def removeUser(
      groupDN: String,
      username: String
  ): OptionT[F, LDAPUser] = OptionT {
    (for {
      userEntry <- getUserEntry(username).value
      groupEntry <- getGroupEntry(groupDN).value
    } yield (userEntry, groupEntry)).map {
      case (Some(user), Some(group))
          if group.hasAttribute("member") && group
            .getAttributeValues("member")
            .contains(user.getDN) =>
        connectionFactory()
          .modify(
            groupDN,
            new Modification(ModificationType.DELETE, "member", user.getDN)
          )
        Some(ldapUser(user))
      case _ =>
        None
    }
  }

}
