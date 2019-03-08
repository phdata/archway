package com.heimdali.templates

import cats.effect.IO
import com.heimdali.models._
import com.heimdali.test.fixtures._
import org.scalatest._
import org.scalatest.prop._

import scala.collection.immutable._

class DefaultStructuredWorkspaceGeneratorSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val raw = HiveAllocation(
    "raw_open_sesame",
    "/data/governed/raw/open_sesame",
    1,
    LDAPRegistration(
      "cn=edh_test_raw_open_sesame,ou=heimdali,dc=jotunn,dc=io",
      "edh_test_raw_open_sesame",
      "role_test_raw_open_sesame",
      attributes = defaultLDAPAttributes("cn=edh_test_raw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_test_raw_open_sesame")),
    Some(LDAPRegistration(
      "cn=edh_test_raw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io",
      "edh_test_raw_open_sesame_ro",
      "role_test_raw_open_sesame_ro",
      attributes = defaultLDAPAttributes("cn=edh_test_raw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_test_raw_open_sesame_ro"))))

  val staging = HiveAllocation(
    "staging_open_sesame",
    "/data/governed/staging/open_sesame",
    1,
    LDAPRegistration(
      "cn=edh_test_staging_open_sesame,ou=heimdali,dc=jotunn,dc=io",
      "edh_test_staging_open_sesame",
      "role_test_staging_open_sesame",
      attributes = defaultLDAPAttributes("cn=edh_test_staging_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_test_staging_open_sesame")),
    Some(LDAPRegistration(
      "cn=edh_test_staging_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io",
      "edh_test_staging_open_sesame_ro",
      "role_test_staging_open_sesame_ro",
      attributes = defaultLDAPAttributes("cn=edh_test_staging_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_test_staging_open_sesame_ro"))))

  val modeled = HiveAllocation(
    "modeled_open_sesame",
    "/data/governed/modeled/open_sesame",
    1,
    LDAPRegistration(
      "cn=edh_test_modeled_open_sesame,ou=heimdali,dc=jotunn,dc=io",
      "edh_test_modeled_open_sesame",
      "role_test_modeled_open_sesame",
      attributes = defaultLDAPAttributes("cn=edh_test_modeled_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_test_modeled_open_sesame")),
    Some(LDAPRegistration(
      "cn=edh_test_modeled_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io",
      "edh_test_modeled_open_sesame_ro",
      "role_test_modeled_open_sesame_ro",
      attributes = defaultLDAPAttributes("cn=edh_test_modeled_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_test_modeled_open_sesame_ro"))))

  val yarn = Yarn("root.governed_open_sesame", 1, 1)

  property("governed templates should generate workspaces") {
    val input = StructuredTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, Some(1), Some(1), Some(1))
    val expected = WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(raw, staging, modeled), processing = List(yarn))
    val templateService = new DefaultStructuredWorkspaceGenerator[IO](appConfig)
    val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
    val time = clock.instant()
    actual.copy(requestDate = time) should be(expected.copy(requestDate = time))
  }

}
