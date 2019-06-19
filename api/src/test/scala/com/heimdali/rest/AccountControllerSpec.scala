package com.heimdali.rest

import cats.data._
import cats.effect.{ContextShift, IO}
import com.heimdali.services.{AccountService, FeatureService, FeatureServiceImpl}
import com.heimdali.test.{TestAuthService, TestClusterService}
import com.heimdali.test.fixtures.{HttpTest, _}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest
    with AppContextProvider {

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "AccountController"

  it should "get a token" in new Context {
    val response: IO[Response[IO]] = accountController.basicAuthRoutes.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/token.expected.json")))(jsonDecoder)
  }

  it should "get an auth-type" in new Context {
    val response: IO[Response[IO]] = accountController.noAuthRoutes.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/auth-type.expected.json")))(jsonDecoder)
  }

  it should "get a profile" in new Context {
    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/profile")))
    check(response, Status.Ok, Some(fromResource("rest/account.profile.expected.json")))
  }

  it should "return not found if a personal workspace hasn't been created yet" in new Context {
    accountService.getWorkspace _ expects standardUserDN returning OptionT.none

    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/workspace")))
    check(response, Status.NotFound, Some("Not found"))
  }

  it should "return the workspace if one exists" in new Context {
    accountService.getWorkspace _ expects standardUserDN returning OptionT.some(savedWorkspaceRequest)

    val response = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/workspace")))
    check(response, Status.Ok, Some(defaultResponse))
  }

  it should "create a new workspace" in new Http4sClientDsl[IO] with Context {
    accountService.createWorkspace _ expects infraApproverUser returning OptionT.some(savedWorkspaceRequest)

    val response = accountController.tokenizedRoutes.orNotFound.run(POST(Uri.uri("/workspace")).unsafeRunSync())
    check(response, Status.Created, Some(defaultResponse))
  }

  it should "get a list of feature flags" in new Context {
    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/feature-flags")))
    check(response, Status.Ok, Some(fromResource("rest/feature-flags.expected.json")))(jsonDecoder)
  }

  trait Context {
    val accountService: AccountService[IO] = mock[AccountService[IO]]
    val authService: TestAuthService = new TestAuthService(platformApprover = true)
    val context = genMockContext(
      clusterService = new TestClusterService(),
      featureService = new FeatureServiceImpl(List("feature-x", "feature-y", "feature-z"))
    )

    lazy val accountController: AccountController[IO] = new AccountController[IO](authService, accountService, context)
  }

}
