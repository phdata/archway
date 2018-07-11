package com.heimdali.controller

import java.time.{ Clock, Instant }

import cats.effect.IO
import com.heimdali.clients.HttpTest
import com.heimdali.models._
import com.heimdali.repositories.Manager
import com.heimdali.rest.WorkspaceController
import com.heimdali.services._
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.generic.extras.Configuration
import io.circe.parser._
import io.circe.syntax._
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

  behavior of "Workspace Controller"

  it should "create a workspace" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService()
    val memberService = mock[MemberService[IO]]

    val workspaceService = mock[WorkspaceService[IO]]
    workspaceService.create _ expects * returning IO(savedWorkspaceRequest)

    val restApi = new WorkspaceController(authService, workspaceService, memberService, clock)
    val response = restApi.route.orNotFound.run(POST(uri("/"), fromResource("rest/workspaces.request.actual.json")).unsafeRunSync())
    check(response, Status.Created, Some(defaultResponse))
  }

  it should "list all workspaces" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService()
    val memberService = mock[MemberService[IO]]

    val workspaceService = mock[WorkspaceService[IO]]
    workspaceService.list _ expects * returning IO(List(savedWorkspaceRequest))

    val restApi = new WorkspaceController(authService, workspaceService, memberService, clock)
    val response = restApi.route.orNotFound.run(GET(uri("/")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(defaultResponse)))
  }

  it should "list all members" in new Http4sClientDsl[IO] {
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService()
    val memberService = mock[MemberService[IO]]

    val workspaceService = mock[WorkspaceService[IO]]
    memberService.members _ expects(123, "sesame", Manager) returning IO.pure(List(WorkspaceMember("johndoe", Some(Instant.now(clock)))))

    val restApi = new WorkspaceController(authService, workspaceService, memberService, clock)
    val response = restApi.route.orNotFound.run(GET(uri("/123/sesame/managers")).unsafeRunSync())
    val Right(json) = parse(
      s"""
        | [
        |   {
        |     "username": "johndoe",
        |     "created": "${Instant.now(clock)}"
        |   }
        | ]
      """.stripMargin)
    check(response, Status.Ok, Some(json))
  }

  it should "updated approvals" in new Http4sClientDsl[IO] {
    import io.circe.java8.time._
    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
    val authService = new TestAuthService(riskApprover = true)
    val memberService = mock[MemberService[IO]]

    val instant = Instant.now()

    val workspaceService = mock[WorkspaceService[IO]]
    (workspaceService.approve _)
      .expects(id, Approval(Infra, standardUsername, Instant.now(clock)))
      .returning(IO.pure(approval(instant).copy(id = Some(id))))

    val restApi = new WorkspaceController(authService, workspaceService, memberService, clock)
    val response = restApi.route.orNotFound.run(POST(uri("/123/approve"), Json.obj("role" -> "infra".asJson)).unsafeRunSync())
    check(response, Created, Some(Json.obj("risk" -> Json.obj("approver" -> standardUsername.asJson, "approval_time" -> instant.asJson))))
  }
}
