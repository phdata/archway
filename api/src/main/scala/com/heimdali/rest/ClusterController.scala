package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.services.ClusterService
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class ClusterController[F[_]: Sync](clusterService: ClusterService[F])
  extends Http4sDsl[F] {

  val route: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      for {
        clusters <- clusterService.list
        response <- Ok(clusters.asJson)
      } yield response
  }

}
