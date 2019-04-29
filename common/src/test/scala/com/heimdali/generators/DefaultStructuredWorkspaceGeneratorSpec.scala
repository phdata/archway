package com.heimdali.generators

import cats.effect.IO
import cats.implicits._
import com.heimdali.models._
import com.heimdali.services.{ApplicationRequest, ConfigService}
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.collection.immutable._

class DefaultStructuredWorkspaceGeneratorSpec extends FlatSpec with MockFactory with Matchers {

  behavior of "DefaultStructuredWorkspaceGenerator"

  val raw = HiveAllocation(
    "raw_open_sesame",
    "/data/governed/raw/open_sesame",
    1,
    LDAPRegistration(
      "cn=edh_dev_raw_open_sesame,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_raw_open_sesame",
      "role_dev_raw_open_sesame",
      attributes = defaultLDAPAttributes("cn=edh_dev_raw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_dev_raw_open_sesame")),
    Some(LDAPRegistration(
      "cn=edh_dev_raw_open_sesame_rw,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_raw_open_sesame_rw",
      "role_dev_raw_open_sesame_rw",
      attributes = defaultLDAPAttributes("cn=edh_dev_raw_open_sesame_rw,ou=heimdali,dc=jotunn,dc=io", "edh_dev_raw_open_sesame_rw"))),
    Some(LDAPRegistration(
      "cn=edh_dev_raw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_raw_open_sesame_ro",
      "role_dev_raw_open_sesame_ro",
      attributes = defaultLDAPAttributes("cn=edh_dev_raw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_dev_raw_open_sesame_ro"))))

  val staging = HiveAllocation(
    "staging_open_sesame",
    "/data/governed/staging/open_sesame",
    1,
    LDAPRegistration(
      "cn=edh_dev_staging_open_sesame,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_staging_open_sesame",
      "role_dev_staging_open_sesame",
      attributes = defaultLDAPAttributes("cn=edh_dev_staging_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_dev_staging_open_sesame")),
    Some(LDAPRegistration(
      "cn=edh_dev_staging_open_sesame_rw,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_staging_open_sesame_rw",
      "role_dev_staging_open_sesame_rw",
      attributes = defaultLDAPAttributes("cn=edh_dev_staging_open_sesame_rw,ou=heimdali,dc=jotunn,dc=io", "edh_dev_staging_open_sesame_rw"))),
    Some(LDAPRegistration(
      "cn=edh_dev_staging_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_staging_open_sesame_ro",
      "role_dev_staging_open_sesame_ro",
      attributes = defaultLDAPAttributes("cn=edh_dev_staging_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_dev_staging_open_sesame_ro"))))

  val modeled = HiveAllocation(
    "modeled_open_sesame",
    "/data/governed/modeled/open_sesame",
    1,
    LDAPRegistration(
      "cn=edh_dev_modeled_open_sesame,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_modeled_open_sesame",
      "role_dev_modeled_open_sesame",
      attributes = defaultLDAPAttributes("cn=edh_dev_modeled_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_dev_modeled_open_sesame")),
    Some(LDAPRegistration(
      "cn=edh_dev_modeled_open_sesame_rw,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_modeled_open_sesame_rw",
      "role_dev_modeled_open_sesame_rw",
      attributes = defaultLDAPAttributes("cn=edh_dev_modeled_open_sesame_rw,ou=heimdali,dc=jotunn,dc=io", "edh_dev_modeled_open_sesame_rw"))),
    Some(LDAPRegistration(
      "cn=edh_dev_modeled_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io",
      "edh_dev_modeled_open_sesame_ro",
      "role_dev_modeled_open_sesame_ro",
      attributes = defaultLDAPAttributes("cn=edh_dev_modeled_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_dev_modeled_open_sesame_ro"))))

  val yarn = Yarn("root.governed_open_sesame", 1, 1)

  it should "governed templates should generate workspaces" in {
    val configService = mock[ConfigService[IO]]
    // for tests
    configService.getAndSetNextGid _ expects() returning 123L.pure[IO] repeat 6 times()
    // for logic
    configService.getAndSetNextGid _ expects() returning 123L.pure[IO] repeat 5 times()
    val ldapGenerator = new DefaultLDAPGroupGenerator[IO](configService)
    val appGenerator = new DefaultApplicationGenerator[IO](appConfig, ldapGenerator)
    val topicGenerator = new DefaultTopicGenerator[IO](appConfig, ldapGenerator)
    val templateService = new DefaultStructuredWorkspaceGenerator[IO](appConfig, ldapGenerator, appGenerator, topicGenerator)
    val input = StructuredTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, Some(1), Some(1), Some(1))

    val workspace = WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", standardUserDN, timer.instant, Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(raw, staging, modeled), processing = List(yarn))
    val application = appGenerator.applicationFor(ApplicationRequest("default"), workspace).unsafeRunSync()
    val expected = workspace.copy(applications = List(application))
    val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
    actual.copy(requestDate = timer.instant) should be(expected.copy(requestDate = timer.instant))
  }

}
