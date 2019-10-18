package io.phdata.clients

import cats.effect.IO
import com.unboundid.ldap.sdk._
import io.phdata.config.Password
import io.phdata.itest.fixtures.{IntegrationTest, _}
import io.phdata.models.DistinguishedName
import io.phdata.test.fixtures.defaultLDAPAttributes
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.collection.JavaConverters._

class LDAPClientImplIntegrationSpec
  extends FlatSpec
    with Matchers
    with MockitoSugar
    with LDAPTest
    with IntegrationTest
    with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    ldapConnectionPool.getConnection.delete(groupDN.value)
  }

  val  groupName = "edh_sw_sesame"
  val groupDN = DistinguishedName(s"cn=$groupName,${itestConfig.ldap.groupPath}")
  val userDN = DistinguishedName(s"CN=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}")

  behavior of "LDAPClientImpl"

  it should "find a user by DN" in {
    val maybeUser = lookupClient.findUserByDN(userDN).value.unsafeRunSync()

    maybeUser shouldBe defined
  }

  it should "not find a user by DN when user with that DN does not exist" in {
    val userDN = DistinguishedName(s"cn=NotExistingUserName},${itestConfig.ldap.userPath.get}")
    val maybeUser = lookupClient.findUserByDN(userDN).value.unsafeRunSync()

    maybeUser shouldNot be (defined)
  }

  it should "find a user by username" in {
    val maybeUser = lookupClient.findUserByUserName(systemTestConfig.existingUser).value.unsafeRunSync()

    maybeUser shouldBe defined
  }

  it should "not find a user by username if user with that username does not exist" in {
    val maybeUser = lookupClient.findUserByUserName("notExistingUserName").value.unsafeRunSync()

    maybeUser shouldNot be (defined)
  }

  it should "validate a user without AD group authorization" in {
    val maybeUser = lookupClient.validateUser(systemTestConfig.existingUser, systemTestConfig.existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "not validate a user without AD group authorization" in {
    val maybeUser = lookupClient.validateUser(systemTestConfig.existingUser, Password("invalid-password")).value.unsafeRunSync()
    maybeUser shouldNot be (defined)
  }

  it should "validate a user which is a member AD group" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    val userDN = s"cn=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}"

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    provisioningClient.addUserToGroup(groupDN, DistinguishedName(userDN)).value.unsafeRunSync()

    val customConfigLookupClient = new LDAPClientImpl[IO](itestConfig.ldap.copy(authorizationDN = groupDN.value), _.lookupBinding)

    val maybeUser = customConfigLookupClient.validateUser(systemTestConfig.existingUser, systemTestConfig.existingPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "not validate a user which is not member of AD group" in {
    val customConfigLookupClient = new LDAPClientImpl[IO](itestConfig.ldap.copy(authorizationDN = "cn=invalid-group"), _.lookupBinding)

    val maybeUser = customConfigLookupClient.validateUser(systemTestConfig.existingUser, systemTestConfig.existingPassword).value.unsafeRunSync()
    maybeUser shouldNot be (defined)
  }

  it should "create a group" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    val updated = attributes.patch(attributes.length - 1, List("gidNumber" -> "124"), 1)

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    provisioningClient.createGroup(groupName, updated).unsafeRunSync()

    Option(ldapConnectionPool.getConnection.getEntry(groupDN.value)) shouldBe defined
  }

  it should "delete a group" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    Option(ldapConnectionPool.getConnection.getEntry(groupDN.value)) shouldBe defined

    provisioningClient.deleteGroup(DistinguishedName(groupDN.value)).value.unsafeRunSync()
    Option(ldapConnectionPool.getConnection.getEntry(groupDN.value)) shouldBe None
  }

  it should "update a group's attributes" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    provisioningClient.createGroup(groupName, attributes :+ ("description" -> "lorem ipsum")).unsafeRunSync()

    val entry = Option(ldapConnectionPool.getConnection.getEntry(groupDN.value))

    entry shouldBe defined

    val attribute = Option(entry.get.getAttribute("description"))
    attribute shouldBe defined
    attribute.get.getValue shouldBe "lorem ipsum"
  }

  it should "add a user to a group" in {
    provisioningClient.createGroup(groupName, defaultLDAPAttributes(groupDN, groupName)).unsafeRunSync()
    val initGroupMembers = provisioningClient.groupMembers(groupDN).unsafeRunSync()

    provisioningClient.addUserToGroup(groupDN, userDN).value.unsafeRunSync()
    val finalGroupMembers = provisioningClient.groupMembers(groupDN).unsafeRunSync()

    initGroupMembers shouldBe empty
    finalGroupMembers.size shouldBe 1
    finalGroupMembers.head.distinguishedName shouldBe userDN
  }

  it should "remove a user from a group" in {
    provisioningClient.createGroup(groupName, defaultLDAPAttributes(groupDN, groupName)).unsafeRunSync()
    val initGroupMembers = provisioningClient.groupMembers(groupDN).unsafeRunSync()

    provisioningClient.addUserToGroup(groupDN, userDN).value.unsafeRunSync()
    val addedGroupMembers = provisioningClient.groupMembers(groupDN).unsafeRunSync()

    provisioningClient.removeUserFromGroup(groupDN, userDN).value.unsafeRunSync()
    val finalGroupMembers = provisioningClient.groupMembers(groupDN).unsafeRunSync()

    initGroupMembers shouldBe empty
    addedGroupMembers.size shouldBe 1
    addedGroupMembers.head.distinguishedName shouldBe userDN
    finalGroupMembers shouldBe empty
  }
  
  it should "find all users" in {
    def userDN(username: String) = DistinguishedName(s"cn=$username,${itestConfig.ldap.userPath.get}")

    lookupClient.createGroup(groupName, defaultLDAPAttributes(groupDN, groupName)).unsafeRunSync()
    // FIXME: Currently not working because ldap user "svc_heim_test1" was accidentally deleted, when user is created this should be uncommented
    // lookupClient.addUserToGroup(groupDN, userDN("svc_heim_test1")).value.unsafeRunSync()
    lookupClient.addUserToGroup(groupDN, userDN("svc_heim_test2")).value.unsafeRunSync()

    val result = lookupClient.groupMembers(DistinguishedName(groupDN.value)).unsafeRunSync()

    result.length shouldBe 1
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
    val actual = provisioningClient.groupRequest(groupDN.value, groupName, defaultLDAPAttributes(groupDN, groupName))
    actual.unsafeRunSync().get shouldBe an [AddRequest]
  }

  it should "generate a modify request" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)

    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()

    val actual = provisioningClient.groupRequest(groupDN.value, groupName, attributes.patch(attributes.length, List("something" -> "new"), 0))
    actual.unsafeRunSync().get shouldBe a [ModifyRequest]
  }

  it should "not generate a request" in {
    val attributes = defaultLDAPAttributes(groupDN, groupName)
    provisioningClient.createGroup(groupName, attributes).unsafeRunSync()
    val actual = provisioningClient.groupRequest(groupDN.value, groupName, List())
    actual.unsafeRunSync() should not be defined
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
    val dn = DistinguishedName("cn=john,dc=example,dc=com")
    val expected = LDAPUser("john (johndoe)", "johndoe", dn, Seq.empty, None)

    val user = new SearchResultEntry(dn.value, Seq(
      new Attribute("name", "john"),
      new Attribute("sAMAccountName", "johndoe")
    ).asJava)

    val actual = lookupClient.ldapUser(user)

    actual shouldBe expected
  }

}
