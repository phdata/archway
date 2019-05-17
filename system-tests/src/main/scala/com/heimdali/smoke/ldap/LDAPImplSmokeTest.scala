package com.heimdali.smoke.ldap

import cats.effect.IO
import com.heimdali.clients.{ActiveDirectoryClient, LDAPClientImpl}
import com.heimdali.config.AppConfig
import org.scalatest.{FlatSpec, Matchers}

class LDAPImplSmokeTest(appConfig: AppConfig) extends FlatSpec with Matchers with LDAPTest {

  behavior of "LDAPClientImpl"

  it should "find all users" in {
    val groupDN = s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}"
    def userDN(username: String) = s"cn=$username,${appConfig.ldap.userPath.get}"
    val client = new LDAPClientImpl[IO](appConfig.ldap) with ActiveDirectoryClient[IO]

    client.createGroup("edh_sw_sesame", defaultLDAPAttributes(groupDN, "edh_sw_sesame")).unsafeRunSync()
    client.addUser(groupDN, userDN("benny")).value.unsafeRunSync()
    client.addUser(groupDN, userDN("John Doe")).value.unsafeRunSync()

    val result = client.groupMembers(groupDN).unsafeRunSync()

    ldapConnectionPool(appConfig).getConnection.delete(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")

    result.length shouldBe 2
  }

  it should "find a user" in {
    val userDN = s"cn=$existingUser,${appConfig.ldap.userPath.get}"
    val client = new LDAPClientImpl[IO](appConfig.ldap) with ActiveDirectoryClient[IO]
    val maybeUser = client.findUser(userDN).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "validate a user" in {
    val client = new LDAPClientImpl[IO](appConfig.ldap) with ActiveDirectoryClient[IO]
    val maybeUser = client.validateUser(existingUser, existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

}
