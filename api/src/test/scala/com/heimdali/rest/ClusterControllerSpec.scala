package com.heimdali.rest

import cats.effect.{ContextShift, IO}
import com.heimdali.test.TestClusterService
import com.heimdali.test.fixtures.{HttpTest, _}
import org.http4s._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class ClusterControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest
    with AppContextProvider {

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "Cluster Controller"

  it should "get a list of clusters" in {
    val context = genMockContext(clusterService = new TestClusterService())
    val clusterController = new ClusterController(context)
    val response: IO[Response[IO]] = clusterController.route.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/cluster.json")))
  }
}
