package io.phdata.rest

import cats.effect.IO
import cats.implicits._
import io.phdata.models.Infra
import io.phdata.services.WorkspaceService
import io.phdata.test.TestAuthService
import io.phdata.test.fixtures._
import io.circe.Json
import io.phdata.models.Infra
import io.phdata.services.WorkspaceService
import io.phdata.test.TestAuthService
import io.phdata.test.fixtures.HttpTest
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class OpsControllerSpec extends FlatSpec with MockFactory with Matchers with HttpTest {

  behavior of "Ops controller"

  it should "return a list of users and groups" in new Http4sClientDsl[IO] with Context {
    workspaceService.reviewerList _ expects Infra returning List(searchResult).pure[IO]

    val response = riskController.route.orNotFound.run(GET(Uri.uri("/workspaces")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(searchResultResponse)))
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService(riskApprover = true)
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]

    lazy val riskController: OpsController[IO] = new OpsController[IO](authService, workspaceService)
  }

}
