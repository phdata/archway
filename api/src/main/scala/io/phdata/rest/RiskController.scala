package io.phdata.rest

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import io.circe.syntax._
import io.phdata.models.{Full, Risk, User}
import io.phdata.rest.authentication.TokenAuthService
import io.phdata.services.WorkspaceService
import org.http4s._
import org.http4s.dsl.Http4sDsl

class RiskController[F[_]: Sync](authService: TokenAuthService[F], workspaceService: WorkspaceService[F])
    extends Http4sDsl[F] {

  val route: HttpRoutes[F] =
    authService.tokenRoleAuth(user => user.role == Risk || user.role == Full) {
      AuthedRoutes.of[User, F] {
        case GET -> Root / "workspaces" as _ =>
          for {
            result <- workspaceService.reviewerList(Risk)
            response <- Ok(result.asJson)
          } yield response
      }
    }

}
