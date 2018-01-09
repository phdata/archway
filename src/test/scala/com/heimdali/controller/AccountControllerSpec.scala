package com.heimdali.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.directives.AuthenticationResult
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.heimdali.rest.{AccountController, AuthService}
import com.heimdali.services._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory
    with FailFastCirceSupport {

  import io.circe.generic.auto._

  lazy val secret: String = ConfigFactory.load().getString("rest.secret")

  behavior of "AccountController"

  it should "do something" in {
    val authService = mock[AuthService]
    (authService.validateCredentials _)
      .expects(*)
      .returning(Future(Right(AuthenticationResult.success(Token("", "")))))

    val accountController = new AccountController(authService)
    Get("/account/token") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      responseAs[Token] should be(Token("", ""))
    }
  }

  it should "get a profile" in {
    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", "username"))))

    val accountController = new AccountController(authService)
    Get("/account/profile") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      responseAs[User] should be(User("Dude Doe", "username"))
    }
  }

}