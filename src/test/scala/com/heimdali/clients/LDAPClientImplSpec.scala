package com.heimdali.clients

import cats.effect.IO
import com.heimdali.test.fixtures.LDAPTest
import com.heimdali.config._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class LDAPClientImplSpec extends FlatSpec
  with Matchers
  with MockFactory
  with LDAPTest {

  behavior of "LDAPClientImpl"

  it should "validate a user" in new Context {
    val client = new LDAPClientImpl[IO](config) with OpenLDAPClient[IO]
    val maybeUser = client.validateUser(username, password).value.unsafeRunSync()
    maybeUser shouldBe defined

    maybeUser.get should have {
      'username ("username")
      'name ("Dude Doe")
    }
  }

  it should "create a group" in new Context {
    val client = new LDAPClientImpl[IO](config) with OpenLDAPClient[IO]
    client.createGroup("edh_sw_sesame", s"cn=edh_sw_sesame,$groupDN,$baseDN").value.unsafeRunSync()
    val entry = client.adminConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,$groupDN,$baseDN")
    entry should not be null
  }

  it should "add a user" in new Context {
    val client = new LDAPClientImpl[IO](config) with OpenLDAPClient[IO]
    client.addUser(s"cn=edh_sw_sesame,$groupDN,$baseDN", "username").value.unsafeRunSync()
    val Some(user) = client.getGroupEntry(s"cn=edh_sw_sesame,$groupDN,$baseDN").value.unsafeRunSync()
    user.getAttributeValues("member") should contain(s"cn=username,$userDN,$baseDN")
  }

  it should "find a user" in new Context {
    val client = new LDAPClientImpl[IO](config) with OpenLDAPClient[IO]
    val maybeUser = client.findUser(username).value.unsafeRunSync()
    maybeUser shouldBe defined

    maybeUser.get should have {
      'username ("username")
      'name ("Dude Doe")
    }
  }

  trait Context {
    val config = LDAPConfig("localhost", 389, "dc=jotunn,dc=io", "ou=groups,ou=hadoop,dc=jotunn,dc=io", Some("ou=users,ou=hadoop,dc=jotunn,dc=io"), "cn=admin,dc=jotunn,dc=io", "admin")
  }

}
