package com.heimdali.services

import cats.effect.IO
import com.heimdali.models._
import com.heimdali.test.fixtures._
import org.scalatest._
import prop._
import scala.collection.immutable._

class DefaultTemplateServiceSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

//  implicit val userTemplateArbitrary = Arbitrary(for {
//    dn <- arbitrary[String]
//    username <- arbitrary[String]
//    disk <- option(arbitrary[Int])
//    cores <- option(arbitrary[Int])
//    memory <- option(arbitrary[Int])
//  } yield UserTemplate(dn, username, disk, cores, memory))
//
//  val service = new DefaultTemplateService[IO](appConfig)
//
//  property("user templates should generate workspaces") = forAll { userTemplate: UserTemplate =>
//    val result = service.userWorkspace(userTemplate).unsafeRunSync()
//    userTemplate.cores.isDefined ==> (result.processing.size == 1)
//  }

  val userInput =
    Table(
      ("input", "output"),
      (UserTemplate(standardUserDN, standardUsername, Some(1), Some(1), Some(1)), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveAllocation(s"user_$standardUsername", s"/user/$standardUsername/db", 250, LDAPRegistration(s"cn=user_$standardUsername,ou=heimdali,dc=jotunn,dc=io", s"user_$standardUsername", s"role_user_$standardUsername"), None)), processing = List(Yarn(s"root.user.$standardUsername", 1, 1)))),
      (UserTemplate(standardUserDN, standardUsername, Some(1), None, None), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveAllocation(s"user_$standardUsername", s"/user/$standardUsername/db", 250, LDAPRegistration(s"cn=user_$standardUsername,ou=heimdali,dc=jotunn,dc=io", s"user_$standardUsername", s"role_user_$standardUsername"), None)))),
      (UserTemplate(standardUserDN, standardUsername, None, Some(1), Some(1)), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, processing = List(Yarn(s"root.user.$standardUsername", 1, 1)))),
      (UserTemplate(standardUserDN, standardUsername, None, None, None), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true))
    )

  property("user templates should generate workspaces") {
    val templateService = new DefaultTemplateService[IO](appConfig)
    forAll(userInput) { (input, expected) =>
      val actual: WorkspaceRequest = templateService.userWorkspace(input).unsafeRunSync()
      val time = clock.instant()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

  val sharedInput =
    Table(
      ("input", "output"),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), Some(1), Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(HiveAllocation("sw_open_sesame", "/data/shared_workspaces/open_sesame", 250, LDAPRegistration("cn=edh_sw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame"), Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro")))), processing = List(Yarn("root.sw_open_sesame", 4, 16)), applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io")))),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), Some(1), None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(HiveAllocation("sw_open_sesame", "/data/shared_workspaces/open_sesame", 250, LDAPRegistration("cn=edh_sw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame"), Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro")))), applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io")))),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), None, Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, processing = List(Yarn("root.sw_open_sesame", 4, 16)), applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io")))),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), None, None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io"))))
    )

  property("shared templates should generate workspaces") {
    val templateService = new DefaultTemplateService[IO](appConfig)
    forAll(sharedInput) { (input, expected) =>
      val actual: WorkspaceRequest = templateService.simpleWorkspace(input).unsafeRunSync()
      val time = clock.instant()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

  val raw = HiveAllocation("raw_open_sesame", "/data/governed/raw/open_sesame", 1, LDAPRegistration("cn=edh_test_raw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_test_raw_open_sesame", "role_test_raw_open_sesame"), Some(LDAPRegistration("cn=edh_test_raw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_test_raw_open_sesame_ro", "role_test_raw_open_sesame_ro")))
  val staging = HiveAllocation("staging_open_sesame", "/data/governed/staging/open_sesame", 1, LDAPRegistration("cn=edh_test_staging_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_test_staging_open_sesame", "role_test_staging_open_sesame"), Some(LDAPRegistration("cn=edh_test_staging_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_test_staging_open_sesame_ro", "role_test_staging_open_sesame_ro")))
  val modeled = HiveAllocation("modeled_open_sesame", "/data/governed/modeled/open_sesame", 1, LDAPRegistration("cn=edh_test_modeled_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_test_modeled_open_sesame", "role_test_modeled_open_sesame"), Some(LDAPRegistration("cn=edh_test_modeled_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_test_modeled_open_sesame_ro", "role_test_modeled_open_sesame_ro")))

  val yarn = Yarn("root.governed_open_sesame", 1, 1)

  val governedInput =
    Table(
      ("input", "output"),
      (StructuredTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, Some(1), Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(raw, staging, modeled), processing = List(yarn))),
      (StructuredTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, Some(1), None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(raw, staging, modeled))),
      (StructuredTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, None, Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, processing = List(yarn))),
      (StructuredTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), includeEnvironment = true, None, None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "structured", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false))
    )

  property("governed templates should generate workspaces") {
    val templateService = new DefaultTemplateService[IO](appConfig)
    forAll(governedInput) { (input, expected) =>
      val actual: WorkspaceRequest = templateService.structuredWorkspace(input).unsafeRunSync()
      val time = clock.instant()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

}
