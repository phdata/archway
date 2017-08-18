package com.heimdali.services

import javax.inject.Inject

import com.unboundid.ldap.sdk._
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class LDAPUser(name: String, username: String, password: String, memberships: Seq[String])

trait LDAPClient {
  def findUser(username: String, password: String): Future[Option[LDAPUser]]
}

class LDAPClientImpl @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext)
  extends LDAPClient {

  val ldapConfiguration: Configuration = configuration.get[Configuration]("ldap")

  val connectionPool: LDAPConnectionPool = {
    val server = ldapConfiguration.get[String]("server")
    val port = ldapConfiguration.get[Int]("port")
    val username = ldapConfiguration.get[String]("bind-dn")
    val password = ldapConfiguration.get[String]("bind-password")
    val connections = ldapConfiguration.getOptional[Int]("connections").getOrElse(10)

    val connection = new LDAPConnection(server, port, username, password)
    new LDAPConnectionPool(connection, connections)
  }

  override def findUser(username: String, password: String): Future[Option[LDAPUser]] = Future {
    val dn = s"${ldapConfiguration.get[String]("users-path")},${ldapConfiguration.get[String]("base-dn")}"

    val connection = connectionPool.getConnection()
    connection.search(dn, SearchScope.ONE, Filter.createEqualityFilter("cn", username))
      .getSearchEntries.asScala.toList match {
      case user :: Nil =>
        Try(connection.bind(new SimpleBindRequest(user.getDN, password)))
          .map(_ =>
            Some(LDAPUser(s"${user.getAttributeValue("givenName")} ${user.getAttributeValue("sn")}",
              user.getAttributeValue("cn"),
              user.getAttributeValue("userPassword"),
              Seq.empty[String]))).getOrElse(None)
      case _ => None
    }
  }

}