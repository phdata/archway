package com.heimdali.templates

import cats.effect.IO
import com.heimdali.models._
import com.heimdali.test.fixtures._
import org.scalatest._
import org.scalatest.prop._

import scala.collection.immutable._

class DefaultSimpleTemplateGeneratorSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val sharedInput =
    Table(
      ("input", "output"),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), Some(1), Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(HiveAllocation("sw_open_sesame", "/data/shared_workspaces/open_sesame", 250, LDAPRegistration("cn=edh_sw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame"), Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro")))), processing = List(Yarn("root.sw_open_sesame", 4, 16)), applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io")))),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), Some(1), None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, data = List(HiveAllocation("sw_open_sesame", "/data/shared_workspaces/open_sesame", 250, LDAPRegistration("cn=edh_sw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame"), Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro")))), applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io")))),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), None, Some(1), Some(1)), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, processing = List(Yarn("root.sw_open_sesame", 4, 16)), applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io")))),
      (SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), None, None, None), WorkspaceRequest("Open Sesame", "A brief summary", "A longer description", "simple", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = false, applications = List(Application(standardUserDN, "open_sesame", "default", "ou=heimdali,dc=jotunn,dc=io"))))
    )

  property("shared templates should generate workspaces") {
    val templateService = new DefaultSimpleTemplateGenerator[IO](appConfig)
    forAll(sharedInput) { (input, expected) =>
      val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
      val time = clock.instant()
      actual.copy(requestDate = time) should be(expected.copy(requestDate = time))
    }
  }

}
