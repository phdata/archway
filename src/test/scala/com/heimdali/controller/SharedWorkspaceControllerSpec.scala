package com.heimdali.controller

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.heimdali.models._
import com.heimdali.rest.{AuthService, WorkspaceController}
import com.heimdali.services._
import com.heimdali.test.fixtures._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.generic.extras.Configuration
import io.circe.parser._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.Future

class SharedWorkspaceControllerSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll
    with MockFactory
    with ScalatestRouteTest
    with FailFastCirceSupport
    with BeforeAndAfterEach {

  def stripCreated(json: Json): Json =
    json.hcursor.withFocus(_.mapObject(_.remove("created"))).top.get

  implicit val configuration: Configuration = Configuration.default.withDefaults.withSnakeCaseKeys

  behavior of "ProjectController"

  it should "create a project" in new TestKit(ActorSystem()) {
    val authService = mock[AuthService]
    (authService.validateToken _).expects(*).returning(Future(Some(User("name", standardUsername))))

    val workspaceService = mock[WorkspaceService]
    workspaceService.create _ expects * returning Future(initialSharedWorkspace)

    val restApi = new WorkspaceController(authService, workspaceService)
    Post("/workspaces", defaultRequest) ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(StatusCodes.Created)

      stripCreated(responseAs[Json]) should be(stripCreated(defaultResponse))
    }
  }

  //TODO: Fix read-only field test
  ignore should "not accept read-only fields" in new TestKit(ActorSystem()) {

    val oldDate = LocalDateTime.of(2010, 1, 1, 0, 0, 0)
    val oldDateString = oldDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val wrongUser = "johnsmith"
    val rightUser = "mrright"
    val fakeId = 999
    val Right(json) = parse(
      s"""
         | {
         |   "id": $fakeId,
         |   "name": "${initialSharedWorkspace.name}",
         |   "purpose": "${initialSharedWorkspace.purpose}",
         |   "system_name": "blahblah",
         |   "compliance": {
         |     "pii_data": $piiCompliance,
         |     "phi_data": $phiCompliance,
         |     "pci_data": $pciCompliance
         |   },
         |   "data": {
         |     "location": "/here/we/go",
         |     "requested_size_in_gb": $hdfsRequestedSize,
         |     "actual_gb": 1000
         |   },
         |   "processing": {
         |     "pool_name": "something cool",
         |     "max_cores": $maxCores,
         |     "max_memory_in_gb": $maxMemoryInGB
         |   },
         |   "created": "$oldDateString",
         |   "created_by": "$wrongUser"
         | }""".stripMargin)

    val factory = mockFunction[SharedWorkspace, ActorRef]
    factory expects initialSharedWorkspace returning testActor

    val workspaceService = mock[WorkspaceService]
    val authService = mock[AuthService]
    (authService.validateToken _).expects(*).returning(Future(Some(User("", rightUser))))
    val restApi = new WorkspaceController(authService, workspaceService)

    Post("/workspaces", json) ~>
      addCredentials(OAuth2BearerToken("AbCdEf123456")) ~>
      restApi.route ~>
      check {
        status should be(StatusCodes.Created)

        stripCreated(responseAs[Json]) should be(stripCreated(defaultResponse))
      }
  }

  it should "list all projects" in {
    val factory = mockFunction[SharedWorkspace, ActorRef]

    val workspaceService = mock[WorkspaceService]
    (workspaceService.list _).expects(*).returning(Future(Seq(initialSharedWorkspace)))
    val authService = mock[AuthService]
    (authService.validateToken _).expects(*).returning(Future(Some(User("", standardUsername))))
    val restApi = new WorkspaceController(authService, workspaceService)

    Get("/workspaces") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(StatusCodes.OK)

      stripCreated(responseAs[Json].hcursor.downArray.focus.get) should be(stripCreated(defaultResponse))
    }
  }

  it should "list all members" in {
    val factory = mockFunction[SharedWorkspace, ActorRef]

    val workspaceService = mock[WorkspaceService]
    (workspaceService.members _).expects(123).returning(Future(Seq(WorkspaceMember("johndoe", "John Doe"))))
    val authService = mock[AuthService]
    (authService.validateToken _).expects(*).returning(Future(Some(User("", standardUsername))))
    val restApi = new WorkspaceController(authService, workspaceService)
    val Right(json) = parse(
      """
        | [
        |   {
        |     "username": "johndoe",
        |     "name": "John Doe"
        |   }
        | ]
      """.stripMargin)

    Get("/workspaces/123/members") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(StatusCodes.OK)

      responseAs[Json] should be(json)
    }
  }
}