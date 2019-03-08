package com.heimdali.templates

import cats.effect.IO
import com.heimdali.models._
import com.heimdali.test.fixtures._
import org.scalatest._
import org.scalatest.prop._

import scala.collection.immutable._

class DefaultUserWorkspaceGeneratorSpec extends PropSpec with Matchers {

  property("user templates should generate workspaces") {
    val templateService = new DefaultUserWorkspaceGenerator[IO](appConfig)
    val input = UserTemplate(standardUserDN, standardUsername, Some(1), Some(1), Some(1))
    val expected = WorkspaceRequest(standardUsername, standardUsername, standardUsername, "user", standardUserDN, clock.instant(), Compliance(phiData = false, pciData = false, piiData = false), singleUser = true, data = List(HiveAllocation(s"user_$standardUsername", s"/user/$standardUsername/db", 250, LDAPRegistration(s"cn=user_$standardUsername,ou=heimdali,dc=jotunn,dc=io", s"user_$standardUsername", s"role_user_$standardUsername", attributes = defaultLDAPAttributes(s"cn=user_$standardUsername,ou=heimdali,dc=jotunn,dc=io", s"user_$standardUsername")), None)), processing = List(Yarn(s"root.user.$standardUsername", 1, 1)))

    val actual: WorkspaceRequest = templateService.workspaceFor(input).unsafeRunSync()
    val time = clock.instant()
    actual.copy(requestDate = time) should be(expected.copy(requestDate = time))
  }

}
