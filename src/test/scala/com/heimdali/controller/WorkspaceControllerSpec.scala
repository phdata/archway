package com.heimdali.controller

import java.time.Instant

import cats.effect.IO
import com.heimdali.clients.HttpTest
import com.heimdali.models.{Approval, Infrastructure, WorkspaceMember}
import com.heimdali.repositories.Manager
import com.heimdali.rest.WorkspaceController
import com.heimdali.services._
import com.heimdali.test.fixtures._
import io.circe.parser._
import io.circe.Json
import io.circe.generic.extras.Configuration
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}

class WorkspaceControllerSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with MockFactory
    with BeforeAndAfterEach
    with HttpTest {

  def stripCreated(json: Json): Json =
    json.hcursor.withFocus(_.mapObject(_.remove("request_date"))).top.get

  behavior of "Workspace Controller"

  it should "create a workspace" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService()

    val workspaceService = mock[WorkspaceService[IO]]
    workspaceService.create _ expects * returning IO(savedWorkspaceRequest)

    val restApi = new WorkspaceController(authService, workspaceService)
    val response = restApi.route.orNotFound.run(POST(uri("/"), fromResource("rest/workspaces.request.actual.json")).unsafeRunSync())
    check(response, Status.Created, Some(stripCreated(defaultResponse)))
  }

  it should "list all workspaces" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService()

    val workspaceService = mock[WorkspaceService[IO]]
    workspaceService.list _ expects * returning IO(List(savedWorkspaceRequest))

    val restApi = new WorkspaceController(authService, workspaceService)
    val response = restApi.route.orNotFound.run(GET(uri("/")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(stripCreated(defaultResponse))))
  }

  it should "list all members" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService()

    val workspaceService = mock[WorkspaceService[IO]]
    workspaceService.members _ expects(123, "sesame", Manager) returning IO.pure(List(WorkspaceMember("johndoe", "John Doe")))

    val restApi = new WorkspaceController(authService, workspaceService)
    val response = restApi.route.orNotFound.run(GET(uri("/123/sesame/managers")).unsafeRunSync())
    val Right(json) = parse(
      """
        | [
        |   {
        |     "username": "johndoe",
        |     "name": "John Doe"
        |   }
        | ]
      """.stripMargin)
    check(response, Status.Ok, Some(json))
  }

  it should "updated approvals" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService(riskApprover = true)

    val instant = Instant.now()

    val workspaceService = mock[WorkspaceService[IO]]
    workspaceService.approve _ expects(id, Infrastructure) returning IO.pure(Approval(Infrastructure, standardUsername, instant))

    val restApi = new WorkspaceController(authService, workspaceService)
    val response = restApi.route.orNotFound.run(POST(uri("/123/approval")).unsafeRunSync())
    check(response, Created, Json.obj("risk" -> Json.obj("approver" -> standardUsername.asJson, "approval_time" -> instant.asJson)))
  }
}