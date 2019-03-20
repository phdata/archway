package com.heimdali.clients

import cats.Id
import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.{Attribute, SearchResultEntry}
import scala.collection.JavaConverters._

class ActiveDirectoryClientSpec extends FlatSpec with Matchers {

  behavior of "ActiveDirectoryClientSpec"

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
    val expected = LDAPUser("john", "johndoe", dn, Seq.empty, None)

    val user = new SearchResultEntry(dn, Seq(
      new Attribute("cn", "john"),
      new Attribute("sAMAccountName", "johndoe")
    ).asJava)

    val actual = client.ldapUser(user)

    actual shouldBe expected
  }

  trait Context {

    val client = new LDAPClientImpl[IO](appConfig.ldap) with ActiveDirectoryClient[IO]

  }

}
