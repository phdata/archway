package io.phdata.clients

import io.phdata.itest.fixtures.{IntegrationTest, _}
import io.phdata.models.DistinguishedName
import io.phdata.test.fixtures.defaultLDAPAttributes
import com.unboundid.ldap.sdk._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class LDAPClientImplIntegrationSpec
  extends FlatSpec
    with Matchers
    with MockitoSugar
    with LDAPTest
    with IntegrationTest {

  val  groupName = "edh_sw_sesame"
  val groupDN = s"cn=$groupName,${itestConfig.ldap.groupPath}"

  behavior of "LDAPClientImpl"

  it should "validate a user" in {
    val maybeUser = lookupClient.validateUser(systemTestConfig.existingUser, systemTestConfig.existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "create a group" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    val updated = attributes.patch(attributes.length - 1, List("gidNumber" -> "124"), 1)
    println(updated)

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    provisioningClient.createGroup(groupName, updated).unsafeRunSync()

    Option(ldapConnectionPool.getConnection.getEntry(groupDN)) shouldBe defined
    ldapConnectionPool.getConnection.delete(groupDN)
  }

  it should "delete a group" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    Option(ldapConnectionPool.getConnection.getEntry(groupDN)) shouldBe defined

    provisioningClient.deleteGroup(DistinguishedName(groupDN)).value.unsafeRunSync()
    Option(ldapConnectionPool.getConnection.getEntry(groupDN)) shouldBe None
  }

  it should "update a group's attributes" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    provisioningClient.createGroup(groupName, attributes :+ ("description" -> "lorem ipsum")).unsafeRunSync()

    val entry = Option(ldapConnectionPool.getConnection.getEntry(groupDN))

    entry shouldBe defined
    ldapConnectionPool.getConnection.delete(groupDN)

    val attribute = Option(entry.get.getAttribute("description"))
    attribute shouldBe defined
    attribute.get.getValue shouldBe "lorem ipsum"
  }

  it should "add a user" in {
    val userDN = s"cn=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}"

    provisioningClient.createGroup(groupName, defaultLDAPAttributes(groupDN, groupName)).unsafeRunSync()
    provisioningClient.addUser(groupDN, DistinguishedName(userDN)).value.unsafeRunSync()

    ldapConnectionPool.getConnection.delete(groupDN)
  }

  it should "find a user" in {
    val userDN = DistinguishedName(s"cn=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}")
    val maybeUser = lookupClient.findUser(userDN).value.unsafeRunSync()
    println(maybeUser.get)
    maybeUser shouldBe defined
  }

  it should "find all users" in {
    def userDN(username: String) = s"cn=$username,${itestConfig.ldap.userPath.get}"

    lookupClient.createGroup(groupName, defaultLDAPAttributes(groupDN, groupName)).unsafeRunSync()
    lookupClient.addUser(groupDN, DistinguishedName(userDN("svc_heim_test1"))).value.unsafeRunSync()
    lookupClient.addUser(groupDN, DistinguishedName(userDN("svc_heim_test2"))).value.unsafeRunSync()

    val result = lookupClient.groupMembers(DistinguishedName(groupDN)).unsafeRunSync()

    ldapConnectionPool.getConnection.delete(groupDN)

    result.length shouldBe 2
  }

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
    val actual = provisioningClient.groupRequest(groupDN, groupName, defaultLDAPAttributes(groupDN, groupName))
    actual.unsafeRunSync().get shouldBe an [AddRequest]
  }

  it should "generate a modify request" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()

    val actual = provisioningClient.groupRequest(groupDN, groupName, attributes.patch(attributes.length, List("something" -> "new"), 0))
    actual.unsafeRunSync().get shouldBe a [ModifyRequest]

    ldapConnectionPool.getConnection.delete(groupDN)
  }

  it should "not generate a request" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    val actual = provisioningClient.groupRequest(groupDN, groupName, List())
    actual.unsafeRunSync() should not be defined

    ldapConnectionPool.getConnection.delete(groupDN)
  }

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
    val results = provisioningClient.search(systemTestConfig.existingUser).unsafeRunSync()
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

  it should "create an LDAPUser" in {
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
