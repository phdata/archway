package io.phdata.rest

import cats.effect.IO
import io.phdata.test.fixtures.{HttpTest, _}
import io.phdata.test.{TestAuthService, TestClusterService}
import io.phdata.test.fixtures.{AppContextProvider, HttpTest}
import org.http4s._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ClusterControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest
    with AppContextProvider {

  behavior of "Cluster Controller"

  it should "get a list of clusters" in {
    val authService: TestAuthService = new TestAuthService
    val context = genMockContext(clusterService = new TestClusterService())
    val clusterController = new ClusterController(authService, context.clusterService)
    val response: IO[Response[IO]] = clusterController.route.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/cluster.json")))
  }
}
