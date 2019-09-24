package io.phdata.rest

import cats.effect.IO
import cats.implicits._
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import io.phdata.models.Infra
import io.phdata.services.{CustomLinkGroupService, WorkspaceService}
import io.phdata.test.TestAuthService
import io.phdata.test.fixtures.{HttpTest, _}
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class OpsControllerSpec extends FlatSpec with MockFactory with Matchers with HttpTest {

  behavior of "Ops controller"

  it should "return a list of users and groups" in new Http4sClientDsl[IO] with Context {
    workspaceService.reviewerList _ expects Infra returning List(searchResult).pure[IO]

    val response = restApi.route.orNotFound.run(GET(Uri.uri("/workspaces")).unsafeRunSync())
    check(response, Status.Ok, Some(Json.arr(searchResultResponse)))
  }

  it should "list all custom links" in new Http4sClientDsl[IO] with Context {
    customLinkService.list _ expects() returning List(customLinkGroup(1L.some, 1L.some, 2L.some)).pure[IO]

    val Right(json) = parse (
      s"""
         |[
         |  {
         |    "id": 1,
         |    "name" : "First group",
         |    "description" : "First custom link group",
         |    "links" : [
         |      {
         |        "id" : 1,
         |        "name" : "First link",
         |        "description" : "First custom link",
         |        "url" : "http://localhost",
         |        "customLinkGroupId" : 1
         |      },
         |      {
         |        "id" : 2,
         |        "name" : "Second link",
         |        "description" : "Second custom link",
         |        "url" : "http://localhost",
         |        "customLinkGroupId" : 1
         |      }
         |    ]
         |  }
         |]
      """.stripMargin)

    println(json)

    val response = restApi.route.orNotFound.run(GET(Uri.uri("custom-links")).unsafeRunSync())
    check(response, Status.Ok, Some(json))
  }

  it should "create a new custom link group" in new Http4sClientDsl[IO] with Context {
    customLinkService.createCustomLinkGroup _ expects customLinkGroup() returning 1L.pure[IO]

    val request = customLinkGroup().asJson
    val response = restApi.route.orNotFound.run(POST(request, Uri.uri("custom-links")).unsafeRunSync()).unsafeRunSync()
    response.status.code shouldBe 201
  }

  it should "update an existing custom link group" in new Http4sClientDsl[IO] with Context {
    customLinkService.updateCustomLinkGroup _ expects (1L, customLinkGroup(1L.some, 1L.some, 2L.some).copy(name="Updated")) returning ().pure[IO]

    val request = customLinkGroup(1L.some, 1L.some, 2L.some).copy(name="Updated").asJson
    val response = restApi.route.orNotFound.run(PUT(request, Uri.uri("custom-links/1")).unsafeRunSync()).unsafeRunSync()

    response.status.code shouldBe 200
  }

  it should "delete an existing custom link group" in new Http4sClientDsl[IO] with Context {
    customLinkService.deleteCustomLinkGroup _ expects 1L returning ().pure[IO]

    val response = restApi.route.orNotFound.run(DELETE(Uri.uri("custom-links/1")).unsafeRunSync()).unsafeRunSync()
    response.status.code shouldBe 200
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService(riskApprover = true)
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]
    val customLinkService: CustomLinkGroupService[IO] = mock[CustomLinkGroupService[IO]]

    lazy val restApi: OpsController[IO] = new OpsController[IO](authService, workspaceService, customLinkService)
  }

}
