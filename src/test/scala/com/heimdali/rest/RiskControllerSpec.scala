package com.heimdali.rest

import cats.implicits._
import cats.effect.IO
import com.heimdali.models.Risk
import com.heimdali.services.WorkspaceService
import com.heimdali.test.fixtures._
import io.circe.Json
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.syntax._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class RiskControllerSpec extends FlatSpec with MockFactory with Matchers with HttpTest {

  behavior of "Risk controller"

  it should "return a list of users and groups" in new Http4sClientDsl[IO] with Context {
    workspaceService.reviewerList _ expects Risk returning List(searchResult).pure[IO]

    val response = riskController.route.orNotFound.run(GET(Uri.uri("/workspaces")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(searchResultResponse)))
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService(riskApprover = true)
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]

    lazy val riskController: RiskController = new RiskController(authService, workspaceService)
  }

}
