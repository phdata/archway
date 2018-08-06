package com.heimdali.clients

import cats.effect.IO
import com.heimdali.test.fixtures.LDAPTest
import com.heimdali.config._
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.{ BindResult, ResultCode }
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import com.unboundid.ldap.sdk.LDAPConnection
import org.scalamock.scalatest.MockFactory
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import com.unboundid.ldap.sdk._
import scala.collection.JavaConverters._

class LDAPClientImplSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with LDAPTest {

  behavior of "LDAPClientImpl"

  ignore should "validate a user" in new Context {
    val bindResult = mock[BindResult]

    val connection = mock[LDAPConnection]
    when(connection.bind(ArgumentMatchers.eq(s"$username@JOTUNN.IO"), ArgumentMatchers.eq(password))).thenReturn(bindResult)

    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]
    val maybeUser = client.validateUser(username, password).value.unsafeRunSync()
  }

  it should "create a group" in new Context {
    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]

    val gidNumberEntry = new SearchResultEntry(client.guidNumberDN, List[Attribute](new Attribute("msSFU30MaxGidNumber", "123")).asJava)

    val connection = mock[LDAPConnection]
    when(connection.getEntry(client.guidNumberDN)).thenReturn(gidNumberEntry)

    client
      .createGroup(id, "edh_sw_sesame", s"cn=edh_sw_sesame,$groupDN,$baseDN")
      .value
      .unsafeRunSync()

    verify(connection).add(ArgumentMatchers.anyVararg().asInstanceOf[String])
  }

  ignore should "add a user" in new Context {
    val userEntry = new SearchResultEntry(
      s"cn=$username,$userDN,$baseDN",
      List(new Attribute("cn", "Dude Doe")).asJava
    )
    val userEntryResult = mock[SearchResult]
    when(userEntryResult.getSearchEntries()).thenReturn(List(userEntry).asJava)

    val groupEntry = new SearchResultEntry(ldapDn, List[Attribute]().asJava)

    val connection = mock[LDAPConnection]
    when(
      connection.search(
        ArgumentMatchers.eq(baseDN),
        ArgumentMatchers.eq(SearchScope.SUB),
        ArgumentMatchers.eq(s"(sAMAccountName=$username)")
      )
    ).thenReturn(userEntryResult)
    when(connection.getEntry(ArgumentMatchers.eq(ldapDn)))
      .thenReturn(groupEntry)

    val client = new LDAPClientImpl[IO](config, () => connection) with ActiveDirectoryClient[IO]
    client.addUser(ldapDn, username).value.unsafeRunSync()

    verify(connection).modify(
      ldapDn,
      new Modification(ModificationType.ADD, "member", userEntry.getDN)
    )
  }

  ignore should "find a user" in new Context {
    val userEntry =
      new SearchResultEntry("", List(new Attribute("cn", "Dude Doe")).asJava)
    val userEntryResult = mock[SearchResult]
    when(userEntryResult.getSearchEntries()).thenReturn(List(userEntry).asJava)
    val connection = mock[LDAPConnection]
    when(
      connection.search(
        ArgumentMatchers.eq(baseDN),
        ArgumentMatchers.eq(SearchScope.SUB),
        ArgumentMatchers.eq(s"(sAMAccountName=$username)")
      )
    ).thenReturn(userEntryResult)
    val client = new LDAPClientImpl[IO](config, () => connection)
    with ActiveDirectoryClient[IO]
    val maybeUser = client.findUser(username).value.unsafeRunSync()
    maybeUser shouldBe defined

    maybeUser.get should have {
      'username ("username")
      'name ("Dude Doe")
    }
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
