package com.heimdali.controller

import cats.effect.IO
import com.heimdali.clients.HttpTest
import com.heimdali.rest.ClusterController
import com.heimdali.services._
import org.http4s.circe._
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
    val clusterService = mock[ClusterService[IO]]
    (clusterService.list _).expects().returning(IO.pure(Seq(Cluster("cluster", "Odin", Map(
      "IMPALA" -> HostClusterApp("impl31", "Impala", "GOOD", "STARTED", "impala.example.com")
    ), CDH("5.11"), "GOOD_HEALTH"))))

    val clusterController = new ClusterController(clusterService)
    val response: IO[Response[IO]] = clusterController.route.orNotFound.run(Request(uri = Uri.uri("/")))
    check(response, Status.Ok, Some(fromResource("rest/cluster.json")))
  }

}
