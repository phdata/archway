package com.heimdali.controller

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.server.directives.AuthenticationResult
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import com.heimdali.models._
import com.heimdali.provisioning.WorkspaceProvisioner.Start
import com.heimdali.rest.{AccountController, AuthService}
import com.heimdali.services._
import com.heimdali.test.fixtures._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import io.circe.generic.extras.Configuration
import io.circe.parser._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory
    with FailFastCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  import io.circe.generic.extras.auto._

  val config = ConfigFactory.load()
  lazy val secret: String = config.getString("rest.secret")

  behavior of "AccountController"

  it should "get a token" in {
    val accountService = mock[AccountService]
    val authService = mock[AuthService]
    (authService.validateCredentials _)
      .expects(*)
      .returning(Future(Right(AuthenticationResult.success(Token("abc", "abc")))))

    val Right(expected) = parse(
      """
        | {
        |   "access_token":"abc",
        |   "refresh_token":"abc"
        | }""".stripMargin)

    val accountController = new AccountController(authService, accountService, config)
    Get("/account/token") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      responseAs[Json] should be(expected)
    }
  }

  it should "get a profile" in {
    val accountService = mock[AccountService]
    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", "username"))))

    val accountController = new AccountController(authService, accountService, config)
    Get("/account/profile") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      responseAs[User] should be(User("Dude Doe", "username"))
    }
  }

  it should "start provisioning a user workspace" in new TestKit(ActorSystem()) {
    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User("Dude Doe", standardUsername))))

    val accountService = mock[AccountService]
    accountService.createWorkspace _ expects standardUsername returning Future(userWorkspace)

    val accountController = new AccountController(authService, accountService, config)
    Post("/account/workspace") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.Created)
    }
  }

  it should "find a user workspace" in new TestKit(ActorSystem()) {
    val accountService = mock[AccountService]
    (accountService.findWorkspace _)
      .expects(standardUsername)
      .returning(Future(Some(completedUserWorkspace)))

    val authService = mock[AuthService]
    (authService.validateToken _)
      .expects(*)
      .returning(Future(Option(User(name, standardUsername))))

    val accountController = new AccountController(authService, accountService, config)
    Get("/account/workspace") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.OK)
      val Right(expected) = parse(
        s"""
           | {
           |   "username": "$standardUsername",
           |   "database": {
           |     "name": "${completedUserWorkspace.hiveDatabase.get.name}",
           |     "location": "${completedUserWorkspace.hiveDatabase.get.location}",
           |     "role": "${completedUserWorkspace.hiveDatabase.get.role}"
           |   },
           |   "ldap": {
           |      "distinguished_name": "${completedUserWorkspace.ldap.get.distinguishedName}",
           |      "common_name": "${completedUserWorkspace.ldap.get.commonName}"
           |   }
           | }
        """.stripMargin
      )
      responseAs[Json] should be(expected)
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

    val accountController = new AccountController(authService, accountService, config)
    Get("/account/workspace") ~> addCredentials(OAuth2BearerToken("abc123")) ~> accountController.route ~> check {
      status should be(StatusCodes.NotFound)
    }
  }

}