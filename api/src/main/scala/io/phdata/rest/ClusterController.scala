package io.phdata.rest

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import io.phdata.models.User
import io.phdata.rest.authentication.TokenAuthService
import io.phdata.services.ClusterService
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl

class ClusterController[F[_]: Sync](authService: TokenAuthService[F], clusterService: ClusterService[F])
    extends Http4sDsl[F] with LazyLogging {

  val route: HttpRoutes[F] = authService.tokenAuth {
    AuthedRoutes.of[User, F] {
      case GET -> Root as _ =>
        for {
          clusters <- clusterService.list.onError {
            case e: Throwable => logger.error(s"Failed to list cluster details: ${e.getLocalizedMessage}", e).pure[F]
          }
          response <- Ok(clusters.asJson)
        } yield response
    }
  }

}
