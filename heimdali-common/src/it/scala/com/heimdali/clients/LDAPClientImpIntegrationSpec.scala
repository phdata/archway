package com.heimdali.clients

import cats.effect._
import com.heimdali.test.fixtures.{LDAPTest, _}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

class LDAPClientImpIntegrationSpec
  extends FlatSpec
    with Matchers
    with MockitoSugar
    with LDAPTest {

  behavior of "LDAPClientImpl"

  it should "validate a user" in {
    val client = new LDAPClientImpl[IO](appConfig.ldap, () => ldapConnectionPool.getConnection) with ActiveDirectoryClient[IO]
    val maybeUser = client.validateUser(existingUser, existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "create a group" in {
    val client = new LDAPClientImpl[IO](appConfig.ldap, () => ldapConnectionPool.getConnection) with ActiveDirectoryClient[IO]

    client.createGroup(10000, "edh_sw_sesame", s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}").value.unsafeRunSync()

    Option(ldapConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")) shouldBe defined
    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")
  }

  it should "add a user" in {
    val groupDN = s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}"
    val userDN = s"cn=$existingUser,${appConfig.ldap.userPath.get}"

    val client = new LDAPClientImpl[IO](appConfig.ldap, () => ldapConnectionPool.getConnection) with ActiveDirectoryClient[IO]

    client.createGroup(10000, "edh_sw_sesame", groupDN).value.unsafeRunSync()
    client.addUser(groupDN, userDN).value.unsafeRunSync()

    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")
  }

  it should "find a user" in {
    val userDN = s"cn=$existingUser,${appConfig.ldap.userPath.get}"
    val client = new LDAPClientImpl[IO](appConfig.ldap, () => ldapConnectionPool.getConnection) with ActiveDirectoryClient[IO]
    val maybeUser = client.findUser(userDN).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "find all users" in {
    val groupDN = s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}"
    def userDN(username: String) = s"cn=$username,${appConfig.ldap.userPath.get}"
    val client = new LDAPClientImpl[IO](appConfig.ldap, () => ldapConnectionPool.getConnection) with ActiveDirectoryClient[IO]

    client.createGroup(10000, "edh_sw_sesame", groupDN).value.unsafeRunSync()
    client.addUser(groupDN, userDN("benny")).value.unsafeRunSync()
    client.addUser(groupDN, userDN("John Doe")).value.unsafeRunSync()

    val result = client.groupMembers(groupDN).unsafeRunSync()

    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")

    result.length shouldBe 2
  }

}
