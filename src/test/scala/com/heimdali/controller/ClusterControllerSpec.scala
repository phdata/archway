package com.heimdali.controller

import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.heimdali.HeimdaliAPI
import com.heimdali.services._
import com.heimdali.startup.Startup
import com.heimdali.test.fixtures.{FakeClusterService, LDAPTest, PassiveAccountService, TestStartup}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ClusterControllerSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory
    with FailFastCirceSupport
    with LDAPTest {

  behavior of "Configuration Controller"

  it should "get a list of clusters" in {
    val clusterService = mock[ClusterService]
    val projectService = mock[ProjectService]
    val accountService = mock[AccountService]
    val restApi = new HeimdaliAPI(clusterService, projectService, accountService)

    Get("/clusters") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(200)

      val Some(Vector(result)) = responseAs[Json].asArray
      (result \\ "name") should be ("admin")
      (result \\ "id") should be ("admin")
      result.hcursor
        .downField("distribution")
        .get[String]("name").toOption.get should be (FakeClusterService.cdh.getClass.getSimpleName)

      result.hcursor
        .downField("distribution")
        .get[String]("version").toOption.get should be (FakeClusterService.cdh.version)
    }
  }

}
