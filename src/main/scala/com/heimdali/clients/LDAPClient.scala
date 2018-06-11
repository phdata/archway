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

case class LDAPUser(name: String, username: String, memberships: Seq[String])

trait LDAPClient[F[_]] {
  def findUser(username: String): OptionT[F, LDAPUser]

  def validateUser(username: String, password: String): OptionT[F, LDAPUser]

  def createGroup(groupName: String, groupDN: String): EitherT[F, _ <: GroupCreationError, Unit]

  def addUser(groupName: String, username: String): OptionT[F, LDAPUser]

  def removeUser(groupName: String, username: String): OptionT[F, LDAPUser]

  def groupMembers(groupDN: String): F[List[LDAPUser]]
}

abstract class LDAPClientImpl[F[_] : Effect](val ldapConfig: LDAPConfig)

  extends LDAPClient[F] with LazyLogging {
  private val marker = MarkerFactory.getMarker("LDAP")

  def searchQuery(username: String): String

  def fullUsername(username: String): String

  def ldapUser(searchResultEntry: SearchResultEntry): LDAPUser

  def groupObjectClass: String

  val connectionPool: LDAPConnectionPool = {
    val connection = new LDAPConnection(ldapConfig.server, ldapConfig.port)
    new LDAPConnectionPool(connection, 10)
  }


  val adminConnectionPool: LDAPConnectionPool = {
    logger.info(marker, "logging into ldap with {}", ldapConfig.bindDN)
    val connection = new LDAPConnection(ldapConfig.server, ldapConfig.port, ldapConfig.bindDN, ldapConfig.bindPassword)
    new LDAPConnectionPool(connection, 10)
  }

  def getUserEntry(username: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay {
      val searchResult = adminConnectionPool.search(ldapConfig.baseDN, SearchScope.SUB, searchQuery(username))
      searchResult
        .getSearchEntries
        .asScala
        .headOption
    })

  def getGroupEntry(dn: String): OptionT[F, SearchResultEntry] =
    OptionT(Sync[F].delay(Try(adminConnectionPool.getEntry(dn)).toOption))

  override def findUser(username: String): OptionT[F, LDAPUser] =
    getUserEntry(username).map(ldapUser)

  override def validateUser(username: String, password: String): OptionT[F, LDAPUser] =
    for {
      bindResult <- OptionT(Sync[F].delay(Try(connectionPool.bindAndRevertAuthentication(fullUsername(username), password)).toOption))
      result <- if (bindResult.getResultCode == ResultCode.SUCCESS) getUserEntry(username).map(ldapUser) else OptionT[F, LDAPUser](Sync[F].pure(None))
    } yield result

  override def createGroup(groupName: String, groupDN: String): EitherT[F, _ <: GroupCreationError, Unit] =
    EitherT(Sync[F].delay {
      try {
        adminConnectionPool.add(s"dn: $groupDN",
          s"objectClass: $groupObjectClass",
          "objectClass: top",
          s"sAMAccountName: $groupName",
          s"cn: $groupName"
        )
        Right(())
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

  override def addUser(groupDN: String, username: String): OptionT[F, LDAPUser] = OptionT {
    (for {
      userEntry <- getUserEntry(username).value
      groupEntry <- getGroupEntry(groupDN).value
    } yield (userEntry, groupEntry)).map {
      case (Some(user), Some(group)) if !group.hasAttribute("member") || !group.getAttributeValues("member").contains(user.getDN) =>
        adminConnectionPool.modify(groupDN, new Modification(ModificationType.ADD, "member", user.getDN))
        Some(ldapUser(user))
      case _ =>
        None
    }
  }

  override def groupMembers(groupDN: String): F[List[LDAPUser]] =
    Sync[F].delay {
      val searchResult = adminConnectionPool.search(ldapConfig.baseDN, SearchScope.SUB, s"(&(objectClass=user)(memberOf=$groupDN))")
      searchResult
        .getSearchEntries
        .asScala
        .map(ldapUser)
        .toList
    }

  override def removeUser(groupDN: String, username: String): OptionT[F, LDAPUser] = OptionT {
    (for {
      userEntry <- getUserEntry(username).value
      groupEntry <- getGroupEntry(groupDN).value
    } yield (userEntry, groupEntry)).map {
      case (Some(user), Some(group)) if group.hasAttribute("member") && group.getAttributeValues("member").contains(user.getDN) =>
        adminConnectionPool.modify(groupDN, new Modification(ModificationType.DELETE, "member", user.getDN))
        Some(ldapUser(user))
      case _ =>
        None
    }
  }
}