package io.phdata.clients

import java.util.UUID

import cats.effect.IO
import io.phdata.itest.fixtures.{HiveTest, IntegrationTest, _}
import io.phdata.models.{Manager, ReadOnly, ReadWrite}
import io.phdata.services.UGILoginContextProvider
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import cats.implicits._

class SentryClientIntegrationSpec extends FlatSpec with Matchers with HiveTest with IntegrationTest with KerberosTest with BeforeAndAfterAll{

  val hiveClient = new HiveClientImpl[IO](new UGILoginContextProvider(itestConfig), hiveTransactor)
  val sentryClient = new SentryClientImpl[IO](hiveTransactor, null, new UGILoginContextProvider(itestConfig))

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
     roles <- sentryClient.roles
     _ <- roles.filter(_.startsWith("test_role_")).map(testRole => sentryClient.dropRole(testRole)).pure[IO]
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
      sentryClient.createRole(role).unsafeRunSync()
    )

    val result = sentryClient.roles.unsafeRunSync()

    result should not be empty
    testRoles.foreach(role =>
      result should contain(role)
    )

    testRoles.foreach(role =>
      sentryClient.dropRole(role).unsafeRunSync()
    )
  }

  it should "create new role" in {
    sentryClient.createRole(role).unsafeRunSync()

    val result = sentryClient.roles.unsafeRunSync()
    result should contain(role)
  }

  it should "drop role" in {
    sentryClient.createRole(role).unsafeRunSync()
    val createdResult = sentryClient.roles.unsafeRunSync()

    sentryClient.dropRole(role).unsafeRunSync()
    val deletedResult = sentryClient.roles.unsafeRunSync()

    createdResult should contain(role)
    deletedResult shouldNot contain(role)
  }

  it should "grant group" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.grantGroup(group, role).unsafeRunSync()

    val result = sentryClient.groupRoles(group).unsafeRunSync()

    sentryClient.revokeGroup(group, role).unsafeRunSync()
    result should contain(role)
  }

  it should "revoke group" in {
    sentryClient.createRole(role).unsafeRunSync()

    sentryClient.grantGroup(group, role).unsafeRunSync()
    val grantedResult = sentryClient.groupRoles(group).unsafeRunSync()

    sentryClient.revokeGroup(group, role).unsafeRunSync()
    val resultRevoked = sentryClient.groupRoles(group).unsafeRunSync()

    grantedResult should contain(role)
    resultRevoked shouldNot contain(role)
  }

  it should "enable manager access to database" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToDB(testDatabase, role, Manager).unsafeRunSync()
    val result = sentryClient.showRoleGrants(role).unsafeRunSync().head
    sentryClient.removeAccessToDB(testDatabase, role, Manager).unsafeRunSync()

    result.database shouldBe testDatabase
    result.grantOption shouldBe true
    result.privilege shouldBe "*"
  }

  it should "remove manager access to database" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToDB(testDatabase, role, Manager).unsafeRunSync()
    val enabledResult = sentryClient.showRoleGrants(role).unsafeRunSync().head

    sentryClient.removeAccessToDB(testDatabase, role, Manager).unsafeRunSync()
    val removedResult = sentryClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe testDatabase
    enabledResult.grantOption shouldBe true
    enabledResult.privilege shouldBe "*"
    removedResult shouldBe empty
  }

  it should "enable readwrite access to database" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()
    val result = sentryClient.showRoleGrants(role).unsafeRunSync().head
    sentryClient.removeAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()

    result.database shouldBe testDatabase
    result.grantOption shouldBe false
    result.privilege shouldBe "*"
  }

  it should "remove readwrite access to database" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()
    val enabledResult = sentryClient.showRoleGrants(role).unsafeRunSync().head

    sentryClient.removeAccessToDB(testDatabase, role, ReadWrite).unsafeRunSync()
    val removedResult = sentryClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe testDatabase
    enabledResult.grantOption shouldBe false
    enabledResult.privilege shouldBe "*"
    removedResult shouldBe empty
  }

  it should "enable readonly access to database" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()
    val result = sentryClient.showRoleGrants(role).unsafeRunSync().head
    sentryClient.removeAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()

    result.database shouldBe testDatabase
    result.grantOption shouldBe false
    result.privilege shouldBe "SELECT"
  }

  it should "remove readonly access to database" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()
    val enabledResult = sentryClient.showRoleGrants(role).unsafeRunSync().head

    sentryClient.removeAccessToDB(testDatabase, role, ReadOnly).unsafeRunSync()
    val removedResult = sentryClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe testDatabase
    enabledResult.grantOption shouldBe false
    enabledResult.privilege shouldBe "SELECT"
    removedResult shouldBe empty
  }

  it should "enable access to location" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToLocation(location, role).unsafeRunSync()
    val result = sentryClient.showRoleGrants(role).unsafeRunSync().head

    result.database shouldBe location
    result.grantOption shouldBe true
    result.privilege shouldBe "*"
  }

  it should "remove access to location" in {
    sentryClient.createRole(role).unsafeRunSync()
    sentryClient.enableAccessToLocation(location, role).unsafeRunSync()
    val enabledResult = sentryClient.showRoleGrants(role).unsafeRunSync().head

    sentryClient.removeAccessToLocation(location, role).unsafeRunSync()
    val removedResult = sentryClient.showRoleGrants(role).unsafeRunSync()

    enabledResult.database shouldBe location
    enabledResult.grantOption shouldBe true
    enabledResult.privilege shouldBe "*"
    removedResult shouldBe empty
  }

}

