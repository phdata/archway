package com.heimdali.rest

import cats.effect._
import com.heimdali.services.ClusterService
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class ClusterController(clusterService: ClusterService[IO]) {

  val route: HttpService[IO] = HttpService {
    case GET -> Root =>
      for {
        clusters <- clusterService.list
        response <- Ok(clusters.asJson)
      } yield response
  }

}
