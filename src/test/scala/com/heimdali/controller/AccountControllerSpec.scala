package com.heimdali.controller

import cats.effect.IO
import com.heimdali.clients.HttpTest
import com.heimdali.rest.AccountController
import com.heimdali.services.AccountService
import com.heimdali.test.fixtures._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{Request, Response, Status, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "AccountController"

  it should "get a token" in {
    val accountService = mock[AccountService[IO]]
    val authService = new TestAuthService

    val accountController = new AccountController(authService, accountService)
    val response: IO[Response[IO]] = accountController.openRoutes.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/token.expected.json")))(jsonDecoder)
  }

  it should "get a profile" in {
    val accountService = mock[AccountService[IO]]
    val authService = new TestAuthService

    val accountController = new AccountController(authService, accountService)
    val response: IO[Response[IO]] = accountController.tokenizedRoutes.orNotFound.run(Request(uri = Uri.uri("/profile")))
    check(response, Status.Ok, Some(fromResource("rest/account.profile.expected.json")))
  }

}