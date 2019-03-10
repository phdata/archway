package com.heimdali.generators

import cats.effect.IO
import com.heimdali.models._
import com.heimdali.test.fixtures._
import org.scalatest._
import org.scalatest.prop._

import scala.collection.immutable._

class DefaultSimpleWorkspaceGeneratorSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  property("shared templates should generate workspaces") {
    val ldapGenerator = new DefaultLDAPGroupGenerator[IO](appConfig)
    val appGenerator = new DefaultApplicationGenerator[IO](appConfig, ldapGenerator)
    val input = SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), Some(1), Some(1), Some(1))
    val workspace = WorkspaceRequest(
      "Open Sesame",
      "A brief summary",
      "A longer description",
      "simple",
      standardUserDN,
      clock.instant(),
      Compliance(phiData = false, pciData = false, piiData = false),
      singleUser = false,
      data = List(
        HiveAllocation(
          "sw_open_sesame",
          "/data/shared_workspaces/open_sesame",
          250,
          LDAPRegistration("cn=edh_sw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame", "role_sw_open_sesame", attributes = defaultLDAPAttributes("cn=edh_sw_open_sesame,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame")),
          Some(LDAPRegistration("cn=edh_sw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame_ro", "role_sw_open_sesame_ro", attributes = defaultLDAPAttributes("cn=edh_sw_open_sesame_ro,ou=heimdali,dc=jotunn,dc=io", "edh_sw_open_sesame_ro"))))),
      processing = List(Yarn("root.sw_open_sesame", 4, 16)))

    val expected = workspace.copy(applications = List(appGenerator.applicationFor("default", workspace).unsafeRunSync()))

    val templateService = new DefaultSimpleWorkspaceGenerator[IO](appConfig, ldapGenerator, appGenerator)

    val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
    val time = clock.instant()
    actual.copy(requestDate = time) should be(expected.copy(requestDate = time))
  }

}
