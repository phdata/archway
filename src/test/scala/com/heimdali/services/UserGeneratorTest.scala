package com.heimdali.services

import java.time.Instant

import com.heimdali.models._
import com.heimdali.services.Generator._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, PropSpec}

class UserGeneratorTest extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val userInput =
    Table(
      ("input", "output"),
      (UserTemplate("johndoe", Some(1), Some(1), Some(1)), WorkspaceRequest("johndoe", "summary", "description", "user", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveDatabase("user_johndoe", "/user/johndoe/db", 250, LDAPRegistration("cn=user_johndoe,ou=groups,ou=hadoop,dc=jotunn,dc=io", "user_johndoe", "role_user_johndoe"), None)), processing = List(Yarn("root.user.johndoe", 1, 1)))),
      (UserTemplate("johndoe", Some(1), None, None), WorkspaceRequest("johndoe", "summary", "description", "user", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveDatabase("user_johndoe", "/user/johndoe/db", 250, LDAPRegistration("cn=user_johndoe,ou=groups,ou=hadoop,dc=jotunn,dc=io", "user_johndoe", "role_user_johndoe"), None)))),
      (UserTemplate("johndoe", None, Some(1), Some(1)), WorkspaceRequest("johndoe", "summary", "description", "user", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, processing = List(Yarn("root.user.johndoe", 1, 1)))),
      (UserTemplate("johndoe", None, None, None), WorkspaceRequest("johndoe", "summary", "description", "user", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true))
    )

  property("user templates should generate workspaces") {
    forAll(userInput) { (input, expected) =>
      val actual: WorkspaceRequest = input.generate()
      val time = Instant.now()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

  val sharedInput =
    Table(
      ("input", "output"),
      (SimpleTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), Some(1), Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(HiveDatabase("sw_open_sesame", "/data/shared_workspaces/open_sesame", 250, LDAPRegistration("cn=edh_sw_open_sesame,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame"), Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro")))), processing = List(Yarn("root.sw_open_sesame", 4, 16)))),
      (SimpleTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), Some(1), None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(HiveDatabase("sw_open_sesame", "/data/shared_workspaces/open_sesame", 250, LDAPRegistration("cn=edh_sw_open_sesame,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame"), Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro")))))),
      (SimpleTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), None, Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, processing = List(Yarn("root.sw_open_sesame", 4, 16)))),
      (SimpleTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), None, None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false))
    )

  property("shared templates should generate workspaces") {
    forAll(sharedInput) { (input, expected) =>
      val actual: WorkspaceRequest = input.generate()
      val time = Instant.now()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

  val raw = HiveDatabase("raw_open_sesame", "/data/governed/raw/open_sesame", 1, LDAPRegistration("cn=edh_test_raw_open_sesame,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_test_raw_open_sesame", "role_test_raw_open_sesame"), Some(LDAPRegistration("cn=edh_test_raw_open_sesame_ro,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_test_raw_open_sesame_ro", "role_test_raw_open_sesame_ro")))
  val staging = HiveDatabase("staging_open_sesame", "/data/governed/staging/open_sesame", 1, LDAPRegistration("cn=edh_test_staging_open_sesame,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_test_staging_open_sesame", "role_test_staging_open_sesame"), Some(LDAPRegistration("cn=edh_test_staging_open_sesame_ro,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_test_staging_open_sesame_ro", "role_test_staging_open_sesame_ro")))
  val modeled = HiveDatabase("modeled_open_sesame", "/data/governed/modeled/open_sesame", 1, LDAPRegistration("cn=edh_test_modeled_open_sesame,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_test_modeled_open_sesame", "role_test_modeled_open_sesame"), Some(LDAPRegistration("cn=edh_test_modeled_open_sesame_ro,ou=groups,ou=hadoop,dc=jotunn,dc=io", "edh_test_modeled_open_sesame_ro", "role_test_modeled_open_sesame_ro")))

  val yarn = Yarn("root.governed_open_sesame", 1, 1)

  val governedInput =
    Table(
      ("input", "output"),
      (StructuredTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, Some(1), Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(raw, staging, modeled), processing = List(yarn))),
      (StructuredTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, Some(1), None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(raw, staging, modeled))),
      (StructuredTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, None, Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, processing = List(yarn))),
      (StructuredTemplate("Open Sesame", "summary", "description", "johndoe", Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, None, None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", "johndoe", Instant.now(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false))
    )

  property("governed templates should generate workspaces") {
    forAll(governedInput) { (input, expected) =>
      val actual: WorkspaceRequest = input.generate()
      val time = Instant.now()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

}
