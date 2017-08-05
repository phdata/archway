package com.heimdali.services

import javax.inject.Inject

import com.unboundid.ldap.sdk._
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

case class LDAPUser(name: String, username: String, password: String, memberships: Seq[String])

trait LDAPClient {
  def findUser(username: String): Future[Option[LDAPUser]]
}

class LDAPClientImpl @Inject()(configuration: Configuration)(implicit executionContext: ExecutionContext)
  extends LDAPClient {

  val ldapConfiguration: Configuration = configuration.get[Configuration]("ldap")

  val connectionPool: LDAPConnectionPool = {
    val connection = new LDAPConnection(ldapConfiguration.get[String]("server"), ldapConfiguration.get[Int]("port"))
    new LDAPConnectionPool(connection, ldapConfiguration.getOptional[Int]("connections").getOrElse(10))
  }

  override def findUser(username: String): Future[Option[LDAPUser]] = Future {
    val dn = s"${ldapConfiguration.get[String]("users-path")},${ldapConfiguration.get[String]("base-dn")}"

    connectionPool.getConnection()
      .search(dn, SearchScope.ONE, Filter.createEqualityFilter("cn", username), "sn", "cn", "givenName", "userPassword")
      .getSearchEntries.asScala.toList match {
      case user :: Nil =>
        Some(LDAPUser(s"${user.getAttributeValue("givenName")} ${user.getAttributeValue("sn")}",
        user.getAttributeValue("cn"),
        user.getAttributeValue("userPassword"),
        Seq.empty[String]))
      case _ => None
    }
  }

}