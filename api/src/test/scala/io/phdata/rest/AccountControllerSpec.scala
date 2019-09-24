package io.phdata.rest

import cats.data._
import cats.effect.IO
import io.phdata.rest.authentication.SpnegoAuthService
import io.phdata.services.AccountService
import io.phdata.test.fixtures.{AppContextProvider, HttpTest, _}
import io.phdata.test.{TestAuthService, TestClusterService}
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest
    with AppContextProvider {

  behavior of "AccountController"

  it should "return a negotiate header" in new SpnegoContext {
    val response: IO[Response[IO]] = accountController.clientAuthRoutes.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Unauthorized, None)
  }

  it should "get a token" in new Context {
    val response: IO[Response[IO]] = accountController.clientAuthRoutes.orNotFound.run(Request(uri = Uri.uri("/")))
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

  it should "get feature flags" in new Context {
    context.featureService.all _ expects() returning List("feature1")
    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/feature-flags")))
    check(response, Status.Ok, Some("[\"feature1\"]"))
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

  trait SharedContext {

    val context = genMockContext(clusterService = new TestClusterService()).copy(featureService = mock[io.phdata.services.FeatureService[IO]])

    val accountService = mock[AccountService[IO]]
    val tokenAuthService = new TestAuthService(platformApprover = true)
  }

  trait Context extends SharedContext {
    val authService = new TestAuthService()
    val accountController = new AccountController[IO](authService, tokenAuthService, accountService, context)
  }

  trait SpnegoContext extends SharedContext {

    val authService = new SpnegoAuthService[IO](accountService)
    val accountController = new AccountController[IO](authService, tokenAuthService, accountService, context)
  }

}
