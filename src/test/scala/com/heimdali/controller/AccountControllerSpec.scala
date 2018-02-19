package com.heimdali.controller

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.directives.AuthenticationResult
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestKit}
import com.heimdali.actors.user.UserProvisioner.{Request, Started}
import com.heimdali.rest.{AccountController, AuthService}
import com.heimdali.services._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.extras.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory
    with FailFastCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withDefaults.withSnakeCaseKeys
  import io.circe.generic.extras.auto._

  lazy val secret: String = ConfigFactory.load().getString("rest.secret")

  behavior of "AccountController"

  it should "do something" in {
    val accountService = mock[AccountService]
    val authService = mock[AuthService]
    (authService.validateCredentials _)
      .expects(*)
      .returning(Future(Right(AuthenticationResult.success(Token("", "")))))

    val accountController = new AccountController(authService, accountService, _ => ActorRef.noSender)
    Get("/account/token") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      responseAs[Token] should be(Token("", ""))
    }
  }

  it should "get a profile" in {
    val accountService = mock[AccountService]
    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", "username"))))

    val accountController = new AccountController(authService, accountService, _ => ActorRef.noSender)
    Get("/account/profile") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      responseAs[User] should be(User("Dude Doe", "username"))
    }
  }

  it should "provision a user workspace" in new TestKit(ActorSystem()) {
    val accountService = mock[AccountService]
    setAutoPilot { (sender: ActorRef, msg: Any) =>
      sender.tell(Started, testActor)
      TestActor.NoAutoPilot
    }

    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", "username"))))

    val accountController = new AccountController(authService, accountService, _ => testActor)

    Post("/account/workspace") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.Created)
      expectMsg(Request)
    }
  }

  it should "find a user workspace" in new TestKit(ActorSystem()) {
    val (username, database, dir, role) = ("username", "username", "/data", "role")
    val accountService = mock[AccountService]
    (accountService.findWorkspace _)
      .expects(username)
      .returning(Future(Some(UserWorkspace(username, database, dir, role))))

    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", username))))

    val accountController = new AccountController(authService, accountService, _ => ActorRef.noSender)

    Get("/account/workspace") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      val workspace = responseAs[UserWorkspace]
      workspace should have(
        'database (database),
        'dataDirectory (dir),
        'role (role)
      )
    }
  }

  it should "return a 404 if no user workspace exists" in new TestKit(ActorSystem()) {
    val username = "username"
    val accountService = mock[AccountService]
    (accountService.findWorkspace _)
      .expects(username)
      .returning(Future(None))

    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", username))))

    val accountController = new AccountController(authService, accountService, _ => ActorRef.noSender)

    Get("/account/workspace") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.NotFound)
    }
  }

}