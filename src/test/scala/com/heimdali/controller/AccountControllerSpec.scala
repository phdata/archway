package com.heimdali.controller

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.heimdali.HeimdaliAPI
import com.heimdali.services._
import com.heimdali.test.fixtures.LDAPTest
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import pdi.jwt.JwtAlgorithm

class AccountControllerSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory
    with FailFastCirceSupport
    with LDAPTest {

  import io.circe.generic.auto._

  lazy val secret: String = ConfigFactory.load().getString("rest.secret")

  behavior of "AccountController"

  it should "do something" in {
    val clusterService = mock[ClusterService]
    val projectService = mock[ProjectService]
    val accountService = mock[AccountService]
    val restApi = new HeimdaliAPI(clusterService, projectService, accountService)
    Get("/account/profile") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(200)
      responseAs[Token]
    }
  }

  it should "get a profile" in {
    val clusterService = mock[ClusterService]
    val projectService = mock[ProjectService]
    val accountService = mock[AccountService]
    val restApi = new HeimdaliAPI(clusterService, projectService, accountService)

    Get("/account/profile") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(200)
      responseAs[User] should be(User("Dude Doe", "username"))
    }
  }

}