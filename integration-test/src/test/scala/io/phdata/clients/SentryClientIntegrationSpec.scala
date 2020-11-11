package io.phdata.clients

import java.util.UUID

import cats.effect.IO
import io.phdata.itest.fixtures.{HiveTest, IntegrationTest, _}
import io.phdata.models.{Manager, ReadOnly, ReadWrite}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import cats.implicits._

class SentryClientIntegrationSpec extends FlatSpec with Matchers with HiveTest with IntegrationTest with BeforeAndAfterAll{

  val hiveClient = new HiveClientImpl[IO](hiveTransactor)
  val roleClient = new RoleClientImpl[IO](hiveTransactor)

  val role = s"test_role_${UUID.randomUUID().toString.take(8)}"
  val group = "integration_test_group"
  val testDatabase = "sentry_client_integration_test"
  val location = "integration_test"

  override def beforeAll() {
    val init = for {
      existingDBs <- hiveClient.showDatabases()
      _ <- if (!existingDBs.contains(testDatabase)) {
        hiveClient.createDatabase(testDatabase, "/tmp", "a comment", Map("pii_data" -> "true", "pci_data" -> "false"))
      } else {
        ().pure[IO]
      }
    } yield ()

    init.unsafeRunSync()
  }

  override def afterAll() {
    val cleanUp = for {
     roles <- roleClient.roles
     _ <- roles.filter(_.startsWith("test_role_")).map(testRole => roleClient.dropRole(testRole)).pure[IO]
    } yield ()

    cleanUp.unsafeRunSync()
  }

  behavior of "Sentry Client"

  it should "list roles" in {
    val testRoles = List(
      s"test_role_${UUID.randomUUID().toString.take(8)}",
      s"test_role_${UUID.randomUUID().toString.take(8)}",
      s"test_role_${UUID.randomUUID().toString.take(8)}"
    )

    testRoles.foreach(role =>
      roleClient.createRole(role).unsafeRunSync()
    )

    val result = roleClient.roles.unsafeRunSync()

    result should not be empty
    testRoles.foreach(role =>
      result should contain(role)
    )

    testRoles.foreach(role =>
      roleClient.dropRole(role).unsafeRunSync()
    )
  }

  it should "create new role" in {
    roleClient.createRole(role).unsafeRunSync()

    val result = roleClient.roles.unsafeRunSync()
    result should contain(role)
  }

  it should "drop role" in {
    roleClient.createRole(role).unsafeRunSync()
    val createdResult = roleClient.roles.unsafeRunSync()

    roleClient.dropRole(role).unsafeRunSync()
    val deletedResult = roleClient.roles.unsafeRunSync()

    createdResult should contain(role)
    deletedResult shouldNot contain(role)
  }

  it should "grant group" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.grantGroup(group, role).unsafeRunSync()

    val result = roleClient.groupRoles(group).unsafeRunSync()

    roleClient.revokeGroup(group, role).unsafeRunSync()
    result should contain(role)
  }

  it should "revoke group" in {
    roleClient.createRole(role).unsafeRunSync()

    roleClient.grantGroup(group, role).unsafeRunSync()
    val grantedResult = roleClient.groupRoles(group).unsafeRunSync()

    roleClient.revokeGroup(group, role).unsafeRunSync()
    val resultRevoked = roleClient.groupRoles(group).unsafeRunSync()

    grantedResult should contain(role)
    resultRevoked shouldNot contain(role)
  }

  it should "enable manager access to database" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToDB(testDatabase, role, Manager).unsafeRunSync()
    val result = roleClient.showRoleGrants(role).unsafeRunSync().head
    roleClient.removeAccessToDB(testDatabase, role, Manager).unsafeRunSync()

    result.database shouldBe testDatabase
    result.grantOption shouldBe true
    result.privilege shouldBe "*"
  }

  it should "remove manager access to database" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToDB(testDatabase, role, Manager).unsafeRunSync()
    val enabledResult = roleClient.showRoleGrants(role).unsafeRunSync().head

    roleClient.removeAccessToDB(testDatabase, role, Manager).unsafeRunSync()
    val removedResult = roleClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe testDatabase
    enabledResult.grantOption shouldBe true
    enabledResult.privilege shouldBe "*"
    removedResult shouldBe empty
  }

  it should "enable readwrite access to database" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()
    val result = roleClient.showRoleGrants(role).unsafeRunSync().head
    roleClient.removeAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()

    result.database shouldBe testDatabase
    result.grantOption shouldBe false
    result.privilege shouldBe "*"
  }

  it should "remove readwrite access to database" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()
    val enabledResult = roleClient.showRoleGrants(role).unsafeRunSync().head

    roleClient.removeAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()
    val removedResult = roleClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe testDatabase
    enabledResult.grantOption shouldBe false
    enabledResult.privilege shouldBe "*"
    removedResult shouldBe empty
  }

  it should "enable readonly access to database" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()
    val result = roleClient.showRoleGrants(role).unsafeRunSync().head
    roleClient.removeAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()

    result.database shouldBe testDatabase
    result.grantOption shouldBe false
    result.privilege shouldBe "SELECT"
  }

  it should "remove readonly access to database" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()
    val enabledResult = roleClient.showRoleGrants(role).unsafeRunSync().head

    roleClient.removeAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()
    val removedResult = roleClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe testDatabase
    enabledResult.grantOption shouldBe false
    enabledResult.privilege shouldBe "SELECT"
    removedResult shouldBe empty
  }

  it should "enable access to location" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToLocation(location, role).unsafeRunSync()
    val result = roleClient.showRoleGrants(role).unsafeRunSync().head

    result.database shouldBe location
    result.grantOption shouldBe true
    result.privilege shouldBe "*"
  }

  it should "remove access to location" in {
    roleClient.createRole(role).unsafeRunSync()
    roleClient.enableAccessToLocation(location, role).unsafeRunSync()
    val enabledResult = roleClient.showRoleGrants(role).unsafeRunSync().head

    roleClient.removeAccessToLocation(location, role).unsafeRunSync()
    val removedResult = roleClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe location
    enabledResult.grantOption shouldBe true
    enabledResult.privilege shouldBe "*"
    removedResult shouldBe empty
  }

}

