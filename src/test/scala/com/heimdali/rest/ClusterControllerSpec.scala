package com.heimdali.rest

import cats.effect.IO
import com.heimdali.services._
import com.heimdali.test.fixtures.{HttpTest, _}
import org.apache.hadoop.conf.Configuration
import org.http4s.dsl.io._
import org.http4s.{Request, Response, Status, Uri}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ClusterControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "Cluster Controller"

  it should "get a list of clusters" in {
    val clusterService = new CDHClusterService[IO](httpClient, clusterConfig, new Configuration())
    val clusterController = new ClusterController(clusterService)
    val response: IO[Response[IO]] = clusterController.route.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/cluster.json")))
  }

}
