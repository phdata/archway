package com.heimdali.clients

import com.heimdali.itest.fixtures.IntegrationTest
import com.unboundid.ldap.sdk._
import com.heimdali.itest.fixtures._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import com.heimdali.test.fixtures.defaultLDAPAttributes

import scala.collection.JavaConverters._

class LDAPClientImplIntegrationSpec
  extends FlatSpec
    with Matchers
    with MockitoSugar
    with LDAPTest
    with IntegrationTest {

  behavior of "LDAPClientImpl"

  it should "validate a user" in {
    val maybeUser = lookupClient.validateUser(systemTestConfig.existingUser, systemTestConfig.existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "create a group" in {
    val attributes = defaultLDAPAttributes(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}", "edh_sw_sesame")
    val updated = attributes.patch(attributes.length - 1, List("gidNumber" -> "124"), 1)
    println(updated)

    provisioningClient.createGroup("edh_sw_sesame", attributes).unsafeRunSync()
    provisioningClient.createGroup("edh_sw_sesame", updated).unsafeRunSync()

    Option(ldapConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")) shouldBe defined
    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")
  }

  it should "delete a group" in {
    val attributes = defaultLDAPAttributes(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}", "edh_sw_sesame")

    provisioningClient.createGroup("edh_sw_sesame", attributes).unsafeRunSync()
    Option(ldapConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")) shouldBe defined

    provisioningClient.deleteGroup(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}").value.unsafeRunSync()
    Option(ldapConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")) shouldBe None
  }

  it should "update a group's attributes" in {

    val attributes = defaultLDAPAttributes(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}", "edh_sw_sesame")
    provisioningClient.createGroup("edh_sw_sesame", attributes).unsafeRunSync()
    provisioningClient.createGroup("edh_sw_sesame", attributes :+ ("description" -> "lorem ipsum")).unsafeRunSync()

    val entry = Option(ldapConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}"))

    entry shouldBe defined
    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")

    val attribute = Option(entry.get.getAttribute("description"))
    attribute shouldBe defined
    attribute.get.getValue shouldBe "lorem ipsum"
  }

  it should "add a user" in {
    val groupDN = s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}"
    val userDN = s"cn=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}"

    provisioningClient.createGroup("edh_sw_sesame", defaultLDAPAttributes(groupDN, "edh_sw_sesame")).unsafeRunSync()
    provisioningClient.addUser(groupDN, userDN).value.unsafeRunSync()

    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")
  }

  it should "find a user" in {
    val userDN = s"cn=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}"
    val maybeUser = lookupClient.findUser(userDN).value.unsafeRunSync()
    println(maybeUser.get)
    maybeUser shouldBe defined
  }

//  it should "find all users" in {
//    val groupDN = s"edh_sw_sesame,${itestConfig.ldap.groupPath}"
//    def userDN(username: String) = s"cn=$username,${itestConfig.ldap.userPath.get}"
//
//    lookupClient.createGroup("edh_sw_sesame", defaultLDAPAttributes(groupDN, "edh_sw_sesame")).unsafeRunSync()
//    lookupClient.addUser(groupDN, userDN("svc_heim_test1")).value.unsafeRunSync()
//    lookupClient.addUser(groupDN, userDN("svc_heim_test2")).value.unsafeRunSync()
//
//    val result = lookupClient.groupMembers(groupDN).unsafeRunSync()
//
//    ldapConnectionPool.getConnection.delete(s"cn=edh_sw_sesame,${itestConfig.ldap.groupPath}")
//
//    result.length shouldBe 2
//  }

  it should "generate the correct modifications" in {
    val existing = List(
      "delete" -> "not",
      "update" -> "me",
      "ignore" -> "me",
      "ignore" -> "me2"
    )
    val updated = List(
      "update" -> "new",
      "ignore" -> "me2",
      "ignore" -> "me",
      "add" -> "me",
    )
    val result = lookupClient.modificationsFor(existing, updated)

    result should contain theSameElementsAs List(
      new Modification(ModificationType.REPLACE, "update", "new"),
      new Modification(ModificationType.ADD, "add", "me")
    )
  }

  it should "generate a new request" in {
    val groupDN = s"cn=absent_group,${itestConfig.ldap.groupPath}"
    val actual = provisioningClient.groupRequest(groupDN, "absent_group", defaultLDAPAttributes(groupDN, "absent_group"))
    actual.unsafeRunSync().get shouldBe an [AddRequest]
  }

//  it should "generate a modify request" in {
//    val groupDN = s"CN=johndoe,OU=heimdali,DC=jotunn,DC=io"
//    val attributes = defaultLDAPAttributes(groupDN, "user_johndoe")
//    val actual = provisioningClient.groupRequest(groupDN, "user_johndoe", attributes.patch(attributes.length, List("something" -> "new"), 0))
//    actual.unsafeRunSync().get shouldBe a [ModifyRequest]
//  }
//
//  it should "not generate a request" in {
//    val groupDN = s"CN=johndoe,OU=heimdali,DC=jotunn,DC=io"
//    val actual = provisioningClient.groupRequest(groupDN, "user_johndoe", List())
//    actual.unsafeRunSync() should not be defined
//  }

  it should "generate only one attribute for multiple keys" in {
    val input = List(
      "objectClass" -> "group",
      "objectClass" -> "top",
      "cn" -> "user_johnny"
    )
    val expected = List(
      new Attribute("objectClass", "group", "top"),
      new Attribute("cn", "user_johnny")
    )
    val actual = provisioningClient.attributeConvert(input)
    actual should contain theSameElementsAs expected
  }

  it should "search" in {
    val results = provisioningClient.search("johndoe").unsafeRunSync()
    results.users.length should be > 0
    results.users.foreach(println)
  }

  it should "fullUsername" in {
    val expected = "johndoe@PHDATA.IO"

    val actual = lookupClient.fullUsername("johndoe")

    actual shouldBe expected
  }

  it should "searchQuery" in {
    val expected = "(sAMAccountName=johndoe)"

    val actual = lookupClient.searchQuery("johndoe")

    actual shouldBe expected
  }

  it should "groupObjectClass" in {
    val expected = "group"

    val actual = lookupClient.groupObjectClass

    actual shouldBe expected
  }

  it should "ldapUser" in {
    val dn = "cn=john,dc=example,dc=com"
    val expected = LDAPUser("john (johndoe)", "johndoe", dn, Seq.empty, None)

    val user = new SearchResultEntry(dn, Seq(
      new Attribute("name", "john"),
      new Attribute("sAMAccountName", "johndoe")
    ).asJava)

    val actual = lookupClient.ldapUser(user)

    actual shouldBe expected
  }

}
