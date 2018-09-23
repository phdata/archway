package com.heimdali.controller

import java.time.Instant

import cats.effect._
import cats.implicits._
import cats.effect._
import com.heimdali.models._
import com.heimdali.rest.WorkspaceController
import com.heimdali.services._
import com.heimdali.test.fixtures._
import io.circe.Json
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

  it should "create a workspace" in new Http4sClientDsl[IO] with Context {
    workspaceService.create _ expects * returning IO(savedWorkspaceRequest)

    val response = restApi.route.orNotFound.run(POST(uri("/"), fromResource("rest/workspaces.request.actual.json")).unsafeRunSync())
    check(response, Status.Created, Some(defaultResponse))
  }

  it should "list all workspaces" in new Http4sClientDsl[IO] with Context {
    workspaceService.list _ expects * returning IO(List(savedWorkspaceRequest))

    val response = restApi.route.orNotFound.run(GET(uri("/")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(defaultResponse)))
  }

  it should "list all members" in new Http4sClientDsl[IO] with Context {
    memberService.members _ expects id returning IO.pure(List(WorkspaceMemberEntry("johndoe", "John Doe", Some("johndoe@email.com"), List.empty, List.empty, List.empty, List.empty)))

    val response = restApi.route.orNotFound.run(GET(uri("/123/members")).unsafeRunSync())
    val Right(json) = parse(
      s"""
        | [
        |   {
        |     "username": "johndoe",
        |     "name": "John Doe",
        |     "email": "johndoe@email.com",
        |     "data": {},
        |     "processing": {},
        |     "topics": {},
        |     "applications": {}
        |   }
        | ]
      """.stripMargin)
    check(response, Status.Ok, Some(json))
  }

  it should "updated approvals" in new Http4sClientDsl[IO] with Context {
    import io.circe.java8.time._

    (workspaceService.approve _)
      .expects(id, Approval(Infra, standardUsername, Instant.now(clock)))
      .returning(IO.pure(approval(clock.instant).copy(id = Some(id))))

    val response = restApi.route.orNotFound.run(POST(uri("/123/approve"), Json.obj("role" -> "infra".asJson)).unsafeRunSync())
    check(response, Created, Some(Json.obj("risk" -> Json.obj("approver" -> standardUsername.asJson, "approval_time" -> clock.instant.asJson))))
  }

  it should "list yarn applications" in new Http4sClientDsl[IO] with Context {
    workspaceService.yarnInfo _ expects id returning List(YarnInfo("pool", List(YarnApplication("application123", "MyApp")))).pure[IO]

    val response = restApi.route.orNotFound.run(GET(uri("/123/yarn")).unsafeRunSync())
    check(response, Status.Ok, Some(fromResource("rest/workspaces.yarn.expected.json")))
  }

  it should "list hive information" in new Http4sClientDsl[IO] with Context {
    workspaceService.hiveDetails _ expects id returning List(HiveDatabase("test", List(HiveTable("table1")))).pure[IO]

    val response = restApi.route.orNotFound.run(GET(uri("/123/hive")).unsafeRunSync())
    check(response, Ok, Some(fromResource("rest/workspaces.hive.expected.json")))
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService(riskApprover = true)
    val memberService: MemberService[IO] = mock[MemberService[IO]]
    val kafkaService: KafkaService[IO] = mock[KafkaService[IO]]
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]
    val applicationService: ApplicationService[IO] = mock[ApplicationService[IO]]

    def restApi: WorkspaceController = new WorkspaceController(authService, workspaceService, memberService, kafkaService, applicationService, clock)
  }
}
