package com.heimdali.services

import javax.inject.Inject

import com.unboundid.ldap.sdk._
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class LDAPUser(name: String, username: String, password: String, memberships: Seq[String])

object LDAPUser {
  def apply(entry: SearchResultEntry): LDAPUser =
    new LDAPUser(s"${entry.getAttributeValue("givenName")} ${entry.getAttributeValue("sn")}",
      entry.getAttributeValue("cn"),
      entry.getAttributeValue("userPassword"),
      Seq.empty[String])
}

trait LDAPClient {
  def findUser(username: String, password: String): Future[Option[LDAPUser]]

  def createGroup(groupName: String, initialMember: String): Future[String]
}

class LDAPClientImpl @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext)
  extends LDAPClient {

  val ldapConfiguration: Configuration = configuration.get[Configuration]("ldap")

  val usersPath = ldapConfiguration.get[String]("users_path")

  val baseDN = ldapConfiguration.get[String]("base_dn")

  val connectionPool: LDAPConnectionPool = {
    val server = ldapConfiguration.get[String]("server")
    val port = ldapConfiguration.get[Int]("port")
    val username = ldapConfiguration.get[String]("bind_dn")
    val password = ldapConfiguration.get[String]("bind_password")
    val connections = ldapConfiguration.getOptional[Int]("connections").getOrElse(10)

    val connection = new LDAPConnection(server, port, username, password)
    new LDAPConnectionPool(connection, connections)
  }

  override def findUser(username: String, password: String): Future[Option[LDAPUser]] = Future {
    val dn = s"cn=$username,$usersPath,$baseDN"

    val connection = connectionPool.getConnection()
    Try(connection.bind(new SimpleBindRequest(dn, password))) match {
      case Success(_) => Some(LDAPUser(connection.getEntry(dn)))
      case Failure(_) => None
    }
  }

  override def createGroup(groupName: String, initialMember: String): Future[String] = Future {
    val connection = connectionPool.getConnection()
    val dn = s"cn=edh_sw_$groupName,${ldapConfiguration.get[String]("group_path")},${ldapConfiguration.get[String]("base_dn")}"
    val entry = connection.add(
      s"dn: $dn",
      "objectClass: top",
      "objectClass: groupOfNames",
      s"cn: edh_sw_$groupName",
      s"member: cn=$initialMember,$usersPath,$baseDN"
    )
    dn
  }
}