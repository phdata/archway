package com.heimdali.generators

import cats.effect.IO
import cats.implicits._
import com.heimdali.models._
import com.heimdali.services.{ApplicationRequest, ConfigService}
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.prop._

import scala.collection.immutable._

class DefaultSimpleWorkspaceGeneratorSpec extends FlatSpec with MockFactory with Matchers {

  behavior of "DefaultSimpleWorkspaceGenerator"

  it should "shared templates should generate workspaces" in {
    val configService = mock[ConfigService[IO]]
    // twice for test objects
    configService.getAndSetNextGid _ expects () returning 123L.pure[IO] twice()
    // twice for actual call
    configService.getAndSetNextGid _ expects () returning 123L.pure[IO] twice()

    val ldapGenerator = new DefaultLDAPGroupGenerator[IO](appConfig, configService)
    val appGenerator = new DefaultApplicationGenerator[IO](appConfig, ldapGenerator)
    val topicGenerator = new DefaultTopicGenerator[IO](appConfig, ldapGenerator)
    val input = SimpleTemplate("Open Sesame", "A brief summary", "A longer description", standardUserDN, Compliance(phiData = false, pciData = false, piiData = false), Some(1), Some(1), Some(1))
    val workspace = WorkspaceRequest(
      "Open Sesame",
      "A brief summary",
      "A longer description",
      "simple",
      standardUserDN,
      timer.instant,
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

    val application = appGenerator.applicationFor(ApplicationRequest("default"), workspace).unsafeRunSync()
    val expected = workspace.copy(applications = List(application))

    val templateService = new DefaultSimpleWorkspaceGenerator[IO](appConfig, ldapGenerator, appGenerator, topicGenerator)

    val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
    actual.copy(requestDate = timer.instant) should be(expected.copy(requestDate = timer.instant))
  }

}
