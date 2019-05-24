package com.heimdali.clients

import cats.effect.IO
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.{Attribute, SearchResultEntry}
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class LDAPClientSpec extends FlatSpec with Matchers {

  behavior of "LDAPClientSpec"

  it should "fullUsername" in new Context {
    val expected = "johndoe@JOTUNN.IO"

    val actual = client.fullUsername("johndoe")

    actual shouldBe expected
  }

  it should "searchQuery" in new Context {
    val expected = "(sAMAccountName=johndoe)"

    val actual = client.searchQuery("johndoe")

    actual shouldBe expected
  }

  it should "groupObjectClass" in new Context {
    val expected = "group"

    val actual = client.groupObjectClass

    actual shouldBe expected
  }

  it should "ldapUser" in new Context {
    val dn = "cn=john,dc=example,dc=com"
    val expected = LDAPUser("john (johndoe)", "johndoe", dn, Seq.empty, None)

    val user = new SearchResultEntry(dn, Seq(
      new Attribute("name", "john"),
      new Attribute("sAMAccountName", "johndoe")
    ).asJava)

    val actual = client.ldapUser(user)

    actual shouldBe expected
  }

  trait Context {

    val client = new LDAPClientImpl[IO](appConfig.ldap, _.lookupBinding)

  }

}
