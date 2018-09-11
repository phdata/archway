package com.heimdali.controller

import cats._
import cats.data._
import cats.implicits._
import cats.effect.IO
import com.heimdali.clients.HttpTest
import com.heimdali.rest.AccountController
import com.heimdali.services.{AccountService, UserTemplate}
import com.heimdali.test.fixtures._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "AccountController"

  it should "get a token" in new Context {
    val response: IO[Response[IO]] = accountController.openRoutes.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/token.expected.json")))(jsonDecoder)
  }

  it should "get a profile" in new Context {
    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/profile")))
    check(response, Status.Ok, Some(fromResource("rest/account.profile.expected.json")))
  }

  it should "return not found if a personal workspace hasn't been created yet" in new Context {
    accountService.getWorkspace _ expects standardUsername returning OptionT.none

    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/profile/workspace")))
    check(response, Status.NotFound, Some("Not found"))
  }

  it should "return the workspace if one exists" in new Context {
    accountService.getWorkspace _ expects standardUsername returning OptionT.some(savedWorkspaceRequest)

    val response = accountController.tokenizedRoutes.orNotFound.run(Request(uri = uri("/profile/workspace")))
    check(response, Status.Ok, Some(defaultResponse))
  }

  it should "create a new workspace" in new Http4sClientDsl[IO] with Context {
    accountService.createWorkspace _ expects infraApproverUser returning OptionT.some(savedWorkspaceRequest)

    val response = accountController.tokenizedRoutes.orNotFound.run(POST(uri("/profile/workspace")).unsafeRunSync())
    check(response, Status.Created, Some(defaultResponse))
  }

  trait Context {
    val accountService: AccountService[IO] = mock[AccountService[IO]]
    val authService: TestAuthService = new TestAuthService

    lazy val accountController: AccountController = new AccountController(authService, accountService)
  }

}