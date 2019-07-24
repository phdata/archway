package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.rest.authentication.TokenAuthService
import com.heimdali.services._
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class OpsController[F[_]: Sync](authService: TokenAuthService[F], workspaceService: WorkspaceService[F])
    extends Http4sDsl[F] with LazyLogging {

  val route: HttpRoutes[F] =
    authService.tokenRoleAuth(user => user.role == Infra || user.role == Full) {
      AuthedService[User, F] {
        case GET -> Root / "workspaces" as _ =>
          for {
            result <- workspaceService.reviewerList(Infra).onError {
              case e: Throwable => logger.error(s"Failed to ops workspaces: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response
      }
    }

}
