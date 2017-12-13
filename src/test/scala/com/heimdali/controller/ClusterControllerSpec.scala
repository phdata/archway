package com.heimdali.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.heimdali.ClusterController
import com.heimdali.services._
import com.heimdali.test.fixtures.FakeClusterService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class ClusterControllerSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with MockFactory
    with FailFastCirceSupport {

  behavior of "Cluster Controller"

  it should "get a list of clusters" in {
    val clusterService = mock[ClusterService]
    (clusterService.list _).expects().returning(Future(Seq(Cluster("admin", "admin", CDH("1.0")))))

    val restApi = new ClusterController(clusterService)

    Get("/clusters") ~> addCredentials(OAuth2BearerToken("AbCdEf123456")) ~> restApi.route ~> check {
      status should be(StatusCodes.OK)

      val Some(Vector(result)) = responseAs[Json].asArray
      println(result)
      result.hcursor
        .get[String]("id").toOption.get should be("admin")

      result.hcursor
        .get[String]("name").toOption.get should be("admin")

      result.hcursor
        .downField("distribution")
        .get[String]("name").toOption.get should be(FakeClusterService.cdh.getClass.getSimpleName)

      result.hcursor
        .downField("distribution")
        .get[String]("version").toOption.get should be(FakeClusterService.cdh.version)
    }
  }

}
