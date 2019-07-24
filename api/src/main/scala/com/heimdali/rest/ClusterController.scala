package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

class ClusterController[F[_]: Sync](context: AppContext[F]) extends Http4sDsl[F] with LazyLogging {

  val route: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      for {
        clusters <- context.clusterService.list.onError {
          case e: Throwable => logger.error(s"Failed to list cluster details: ${e.getLocalizedMessage}", e).pure[F]
        }
        response <- Ok(clusters.asJson)
      } yield response
  }

}
