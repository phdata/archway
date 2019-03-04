package com.heimdali.templates

import cats.effect.IO
import com.heimdali.models._
import com.heimdali.test.fixtures._
import org.scalatest._
import org.scalatest.prop._

import scala.collection.immutable._

class DefaultUserTemplateGeneratorSpec extends PropSpec with TableDrivenPropertyChecks with Matchers {

  val userInput =
    Table(
      ("input", "output"),
      (UserTemplate(standardUserDN, standardUsername, Some(1), Some(1), Some(1)), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveAllocation(s"user_$standardUsername", s"/user/$standardUsername/db", 250, LDAPRegistration(s"cn=user_$standardUsername,ou=heimdali,dc=jotunn,dc=io", s"user_$standardUsername", s"role_user_$standardUsername"), None)), processing = List(Yarn(s"root.user.$standardUsername", 1, 1)))),
      (UserTemplate(standardUserDN, standardUsername, Some(1), None, None), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveAllocation(s"user_$standardUsername", s"/user/$standardUsername/db", 250, LDAPRegistration(s"cn=user_$standardUsername,ou=heimdali,dc=jotunn,dc=io", s"user_$standardUsername", s"role_user_$standardUsername"), None)))),
      (UserTemplate(standardUserDN, standardUsername, None, Some(1), Some(1)), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, processing = List(Yarn(s"root.user.$standardUsername", 1, 1)))),
      (UserTemplate(standardUserDN, standardUsername, None, None, None), WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true))
    )

  property("user templates should generate workspaces") {
    val templateService = new DefaultUserTemplateGenerator[IO](appConfig)
    forAll(userInput) { (input, expected) =>
      val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
      val time = clock.instant()
      actual.copy(requestDate = time) should be (expected.copy(requestDate = time))
    }
  }

}
