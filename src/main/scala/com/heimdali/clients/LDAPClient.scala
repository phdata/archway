package com.heimdali.clients

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.unboundid.ldap.sdk._
import org.slf4j.MarkerFactory

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class LDAPUser(name: String, username: String, memberships: Seq[String])

trait LDAPClient {
  def findUser(username: String): Future[Option[LDAPUser]]

  def validateUser(username: String, password: String): Future[Option[LDAPUser]]

  def createGroup(groupName: String): Future[String]

  def addUser(group: String, username: String): Future[String]

  def groupMembers(groupDN: String): Future[Seq[LDAPUser]]
}

abstract class LDAPClientImpl(configuration: Config)
                             (implicit executionContext: ExecutionContext)
  extends LDAPClient with LazyLogging {
  val marker = MarkerFactory.getMarker("LDAP")

  def searchQuery(username: String): String

  def fullUsername(username: String): String

  def ldapUser(searchResultEntry: SearchResultEntry): LDAPUser

  def groupObjectClass: String

  val ldapConfiguration: Config = configuration.getConfig("ldap")
  val groupPath: String = ldapConfiguration.getString("group_path")
  val baseDN: String = ldapConfiguration.getString("base_dn")
  val server: String = ldapConfiguration.getString("server")
  val port: Int = ldapConfiguration.getInt("port")
  val connections: Int = Try(ldapConfiguration.getInt("connections")).getOrElse(10)

  val connectionPool: LDAPConnectionPool = {
    val connection = new LDAPConnection(server, port)
    new LDAPConnectionPool(connection, connections)
  }

  def groupDN(groupName: String): String =
    s"cn=$groupName,$groupPath"


  val adminConnectionPool: LDAPConnectionPool = {
    val username = ldapConfiguration.getString("bind_dn")
    val password = ldapConfiguration.getString("bind_password")
    logger.info(marker, "logging into ldap with {}", username)
    val connection = new LDAPConnection(server, port, username, password)
    new LDAPConnectionPool(connection, connections)
  }

  def getUserEntry(username: String): Option[SearchResultEntry] = {
    val searchResult = adminConnectionPool.search(baseDN, SearchScope.SUB, searchQuery(username))
    searchResult
      .getSearchEntries
      .asScala
      .headOption
  }

  override def findUser(username: String): Future[Option[LDAPUser]] = Future {
    getUserEntry(username)
      .map(ldapUser)
  }

  override def validateUser(username: String, password: String): Future[Option[LDAPUser]] = Future {
    Try(connectionPool.bindAndRevertAuthentication(fullUsername(username), password)) match {
      case Success(result) if result.getResultCode == ResultCode.SUCCESS =>
        getUserEntry(username)
          .map(ldapUser)
      case Failure(exc) =>
        exc.printStackTrace()
        None
    }
  }

  override def createGroup(groupName: String): Future[String] = Future {
    val dn = groupDN(groupName)
    try {
      adminConnectionPool.add(
        s"dn: $dn",
        s"objectClass: $groupObjectClass",
        "objectClass: top",
        s"sAMAccountName: $groupName",
        s"cn: $groupName"
      )
      dn
    } catch {
      case exc: LDAPException if exc.getResultCode == ResultCode.ENTRY_ALREADY_EXISTS =>
        logger.warn("group {} already exists", groupName)
        dn
      case exc =>
        logger.error("couldn't create ldap group")
        logger.error(exc.getMessage, exc)
        throw exc
    }
  }

  override def addUser(groupName: String, username: String): Future[String] = Future {
    try {
      val Some(user) = getUserEntry(username)

      val dn = groupDN(groupName)
      val groupEntry = Option(adminConnectionPool.getEntry(dn))

      if (groupEntry.isDefined &&
        groupEntry.get.hasAttribute("member") &&
        groupEntry.get.getAttributeValues("member").contains(user.getDN)) {
        logger.info("{} is already a member of {}", username, groupName)
      } else {
        logger.info("adding user {} to group {}", user, groupName)
        adminConnectionPool.modify(dn, new Modification(ModificationType.ADD, "member", user.getDN))
      }

      dn
    } catch {
      case exc: Throwable =>
        logger.error("couldn't add user", exc)
        throw exc
    }
  }

  override def groupMembers(groupDN: String): Future[Seq[LDAPUser]] = Future {
    val searchResult = adminConnectionPool.search(baseDN, SearchScope.SUB, s"(&(objectClass=user)(memberOf=$groupDN))")
    searchResult.getSearchEntries.asScala.map(ldapUser)
  }
}