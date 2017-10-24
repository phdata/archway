package com.heimdali.test.fixtures

import com.typesafe.config.ConfigFactory
import com.unboundid.ldap.sdk.{LDAPConnection, SearchScope}
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.util.Try

trait LDAPTest extends BeforeAndAfterEach {
  this: Suite =>

  val baseDN = "dc=jotunn,dc=io"
  val userDN = "ou=users,ou=hadoop"
  val bindDN = "cn=readonly,dc=jotunn,dc=io"
  val bindPassword = "readonly"
  val username = "username"
  val password = "password"

  val config = new Configuration(ConfigFactory.load())
  val ldapConnection = new LDAPConnection(config.get[String]("ldap.server"), config.get[Int]("ldap.port"), config.get[String]("ldap.bind_dn"), config.get[String]("ldap.bind_password"))

  override protected def beforeEach(): Unit =
    Try(ldapConnection.add(
        s"dn: cn=$username,$userDN,$baseDN",
        "objectClass: inetOrgPerson",
        "sn: Doe",
        "givenName: Dude",
        "userPassword: password"))

  override protected def afterEach(): Unit = {
    val users = ldapConnection.search("ou=users,ou=hadoop,dc=jotunn,dc=io", SearchScope.SUB, "(objectClass=person)").getSearchEntries.asScala
    val groups = ldapConnection.search("ou=groups,ou=hadoop,dc=jotunn,dc=io", SearchScope.SUB, "(objectClass=groupOfNames)").getSearchEntries.asScala
    (users ++ groups).map(_.getDN).map(ldapConnection.delete)
  }
}
