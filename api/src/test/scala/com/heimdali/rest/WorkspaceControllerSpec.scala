package com.heimdali.rest

import cats.data.{NonEmptyList, OptionT}
import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.provisioning.{Error, SimpleMessage, Message => OurMessage}
import com.heimdali.services._
import com.heimdali.test.TestAuthService
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.http4s.{client, _}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

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
    emailService.newWorkspaceEmail _ expects savedWorkspaceRequest returning IO.unit

    val response = restApi.route.orNotFound.run(POST(fromResource("rest/workspaces.request.actual.json"), Uri.uri("/")).unsafeRunSync())
    check(response, Status.Created, Some(defaultResponse))
  }

  it should "list all workspaces" in new Http4sClientDsl[IO] with Context {
    workspaceService.list _ expects * returning IO(List(searchResult))

    val response = restApi.route.orNotFound.run(GET(Uri.uri("/")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(searchResultResponse)))
  }

  it should "list all members" in new Http4sClientDsl[IO] with Context {
    memberService.members _ expects id returning IO.pure(List(WorkspaceMemberEntry(standardUserDN.value, "John Doe", Some("johndoe@email.com"), List.empty, List.empty, List.empty, List.empty)))

    val response = restApi.route.orNotFound.run(GET(Uri.uri("/123/members")).unsafeRunSync())
    val Right(json) = parse(
      s"""
         | [
         |   {
         |     "distinguished_name": "$standardUserDN",
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
      .expects(id, Approval(Infra, standardUsername, testTimer.instant))
      .returning(IO.pure(approval(testTimer.instant).copy(id = Some(id))))

    val response = restApi.route.orNotFound.run(POST(Json.obj("role" -> "infra".asJson), Uri.uri("/123/approve")).unsafeRunSync())
    check(response, Created, Some(Json.obj("risk" -> Json.obj("approver" -> standardUsername.asJson, "approval_time" -> testTimer.instant.asJson))))
  }

  it should "list yarn applications" in new Http4sClientDsl[IO] with Context {
    workspaceService.yarnInfo _ expects id returning List(YarnInfo("pool", List(YarnApplication("application123", "MyApp")))).pure[IO]

    val response = restApi.route.orNotFound.run(GET(Uri.uri("/123/yarn")).unsafeRunSync())
    check(response, Status.Ok, Some(fromResource("rest/workspaces.yarn.expected.json")))
  }

  it should "list hive information" in new Http4sClientDsl[IO] with Context {
    workspaceService.hiveDetails _ expects id returning List(HiveDatabase("test", List(HiveTable("table1")))).pure[IO]

    val response = restApi.route.orNotFound.run(GET(Uri.uri("/123/hive")).unsafeRunSync())
    check(response, Ok, Some(fromResource("rest/workspaces.hive.expected.json")))
  }

  it should "add a user" in new Http4sClientDsl[IO] with Context {
    val memberRequest = MemberRoleRequest(standardUserDN.value, "data", id, Some(Manager))
    (memberService.addMember _)
      .expects(id, memberRequest)
      .returning(OptionT.some(
        WorkspaceMemberEntry(standardUserDN.value, name, Some("johndoe@phdata.io"), List.empty, List.empty, List.empty, List.empty)
      ))
    (emailService.newMemberEmail _).expects(id, memberRequest).returning(OptionT.some(IO.unit))

    val request = Json.obj(
      "distinguished_name" -> standardUserDN.asJson,
      "resource" -> "data".asJson,
      "resource_id" -> id.asJson,
      "role" -> "manager".asJson
    )

    val response = restApi.route.orNotFound.run(POST(request, Uri.uri("/123/members")).unsafeRunSync()).unsafeRunSync()
    response.status.code shouldBe 201
  }

  it should "return status 500 if adding member fails" in new Http4sClientDsl[IO] with Context {
    val memberRequest = MemberRoleRequest(standardUserDN.value, "data", id, Some(Manager))

    (memberService.addMember _).expects(id, memberRequest).returning(OptionT.none)
    (emailService.newMemberEmail _).expects(id, memberRequest).returning(OptionT.some(IO.unit))

    val request = Json.obj(
      "distinguished_name" -> standardUserDN.asJson,
      "resource" -> "data".asJson,
      "resource_id" -> id.asJson,
      "role" -> "manager".asJson
    )

    val response = restApi.route.orNotFound.run(POST(request, Uri.uri("/123/members")).unsafeRunSync()).unsafeRunSync()
    response.status.code shouldBe 500
  }

  it should "provision workspace" in new Http4sClientDsl[IO] with Context {
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    provisioningService.attemptProvision _ expects(savedWorkspaceRequest, 0) returning NonEmptyList.one(SimpleMessage(id, "nothing to see here").asInstanceOf[OurMessage]).pure[IO].start(contextShift)

    val response = restApi.route.orNotFound.run(POST(Uri.uri("/123/provision")).unsafeRunSync())
    check(response, Status.Created, Some(Json.arr(Json.obj("message" -> "nothing to see here".asJson))))
  }

  it should "return a 500 on a failed provision" in new Http4sClientDsl[IO] with Context {
    val error = Error.message("", 1, new Exception())
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    provisioningService.attemptProvision _ expects(savedWorkspaceRequest, 0) returning error.pure[IO].start(contextShift)

    val response = restApi.route.orNotFound.run(POST(Uri.uri("/123/provision")).unsafeRunSync())
    check(response, Status.InternalServerError, Some(Json.arr(Json.obj("message" -> "FAILED:  for workspace 1 due to null".asJson))))
  }

  it should "deprovision workspace" in new Http4sClientDsl[IO] with Context {
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    provisioningService.attemptDeprovision _ expects(savedWorkspaceRequest) returning NonEmptyList.one(SimpleMessage(id, "nothing to see here").asInstanceOf[OurMessage]).pure[IO].start(contextShift)

    val response = restApi.route.orNotFound.run(POST(Uri.uri("/123/deprovision")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(Json.obj("message" -> "nothing to see here".asJson))))
  }

  it should "return a 500 on a failed deprovision" in new Http4sClientDsl[IO] with Context {
    val error = Error.message("", 1, new Exception())
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    provisioningService.attemptDeprovision _ expects(savedWorkspaceRequest) returning error.pure[IO].start(contextShift)

    val response = restApi.route.orNotFound.run(POST(Uri.uri("/123/deprovision")).unsafeRunSync())
    check(response, Status.InternalServerError, Some(Json.arr(Json.obj("message" -> "FAILED:  for workspace 1 due to null".asJson))))
  }

  it should "delete a workspace" in new client.dsl.Http4sClientDsl[IO] with Context {
    workspaceService.deleteWorkspace _ expects id returning ().pure[IO]
    val response = restApi.route.orNotFound.run(DELETE(Uri.uri("/123")).unsafeRunSync()).unsafeRunSync()

    response.status.code shouldBe 200
  }

  it should "get a workspace status" in new client.dsl.Http4sClientDsl[IO] with Context {
    workspaceService.status _ expects id returning workspaceStatus.pure[IO]
    val response = restApi.route.orNotFound.run(GET(Uri.uri("/123/status")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.obj("provisioning" -> WorkspaceProvisioningStatus.COMPLETED.asJson)))
  }

  trait Context {
    implicit val timer: Timer[IO] = testTimer
    implicit val contextShift = IO.contextShift(ExecutionContext.global)
    implicit val concurrentEffect = IO.ioConcurrentEffect(contextShift)
    val authService: TestAuthService = new TestAuthService(riskApprover = true, platformApprover = true)
    val memberService: MemberService[IO] = mock[MemberService[IO]]
    val kafkaService: KafkaService[IO] = mock[KafkaService[IO]]
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]
    val applicationService: ApplicationService[IO] = mock[ApplicationService[IO]]
    val emailService: EmailService[IO] = mock[EmailService[IO]]
    val provisioningService: ProvisioningService[IO] = mock[ProvisioningService[IO]]

    def restApi: WorkspaceController[IO] =
      new WorkspaceController[IO](
        authService,
        workspaceService,
        memberService,
        kafkaService,
        applicationService,
        emailService,
        provisioningService,
        ExecutionContext.global
      )
  }
}
