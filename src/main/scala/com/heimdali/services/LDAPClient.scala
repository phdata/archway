package com.heimdali.services

import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import com.unboundid.ldap.sdk._
import org.slf4j.{Marker, MarkerFactory}
import org.slf4j.helpers.{BasicMarker, BasicMarkerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class LDAPUser(name: String, username: String, memberships: Seq[String])

trait LDAPClient {
  def findUser(username: String, password: String): Future[Option[LDAPUser]]

  def createGroup(groupName: String, initialMember: String): Future[String]
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
  val usersPath: String = ldapConfiguration.getString("users_path")
  val groupPath: String = ldapConfiguration.getString("group_path")
  val baseDN: String = ldapConfiguration.getString("base_dn")
  val server: String = ldapConfiguration.getString("server")
  val port: Int = ldapConfiguration.getInt("port")
  val connections: Int = Try(ldapConfiguration.getInt("connections")).getOrElse(10)

  val connectionPool: LDAPConnectionPool = {
    val connection = new LDAPConnection(server, port)
    new LDAPConnectionPool(connection, connections)
  }

  val adminConnectionPool: LDAPConnectionPool = {
    val username = ldapConfiguration.getString("bind_dn")
    val password = ldapConfiguration.getString("bind_password")
    logger.info(marker, "logging into ldap with {}/{}", username, password)
    val connection = new LDAPConnection(server, port, username, password)
    new LDAPConnectionPool(connection, connections)
  }

  def getUserEntry(username: String): Option[SearchResultEntry] = {
    val connection = adminConnectionPool.getConnection
    val searchResult = connection.search(s"$usersPath,$baseDN", SearchScope.SUB, searchQuery(username))
    searchResult
      .getSearchEntries
      .asScala
      .headOption
  }

  override def findUser(username: String, password: String): Future[Option[LDAPUser]] = Future {
    Try(connectionPool.bindAndRevertAuthentication(fullUsername(username), password)) match {
      case Success(result) if result.getResultCode == ResultCode.SUCCESS =>
        getUserEntry(username)
          .map(ldapUser)
      case Failure(exc) =>
        exc.printStackTrace()
        None
    }
  }


  override def createGroup(groupName: String, initialMember: String): Future[String] = Future {
    val Some(user) = getUserEntry(initialMember)
    val connection = adminConnectionPool.getConnection()
    val dn = s"cn=$groupName,$groupPath,$baseDN"
    try {
      connection.add(
        s"dn: $dn",
        s"objectClass: $groupObjectClass",
        "objectClass: top",
        s"cn: $groupName",
        s"member: ${user.getDN}"
      )
      dn
    } catch {
      case exc: LDAPException if exc.getResultCode == ResultCode.ENTRY_ALREADY_EXISTS => dn
      case exc => throw exc
    }
  }
}