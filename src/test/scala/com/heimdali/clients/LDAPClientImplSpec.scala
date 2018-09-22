package com.heimdali.clients

import cats._
import cats.implicits._
import cats.effect._
import com.heimdali.test.fixtures.LDAPTest
import com.heimdali.config._
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.{Modification, ModificationType, SearchResultEntry}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

class LDAPClientImplSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with LDAPTest {

  behavior of "LDAPClientImpl"

  it should "validate a user" in new Context {
    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]
    val maybeUser = client.validateUser(existingUser, existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "create a group" in new Context {
    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]

    client.createGroup(id, "edh_sw_sesame", s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}").value.unsafeRunSync()

    Option(connection.getEntry(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")) shouldBe defined
    connection.delete(s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}")
  }

  it should "add a user" in new Context {
    val groupDN = s"cn=edh_sw_sesame,${appConfig.ldap.groupPath}"
    val userDN = s"cn=${existingUser},${appConfig.ldap.userPath}"

    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]

    client.createGroup(id, "edh_sw_sesame", groupDN).value.unsafeRunSync()
    client.addUser(groupDN, existingUser).value.unsafeRunSync()

    connection.modify(ldapDn, new Modification(ModificationType.DELETE, "member", userDN))
  }

  it should "find a user" in new Context {
    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]
    val maybeUser = client.findUser(existingUser).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  trait Context {
    val config = LDAPConfig(
      "localhost",
      389,
      "dc=jotunn,dc=io",
      "ou=groups,ou=hadoop,dc=jotunn,dc=io",
      Some("ou=users,ou=hadoop,dc=jotunn,dc=io"),
      "cn=admin,dc=jotunn,dc=io",
      "admin",
      "jotunn"
    )
  }

}
