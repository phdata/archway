package com.heimdali.rest

import cats.Monad
import cats.implicits._
import com.heimdali.models._
import com.heimdali.rest.authentication.{AuthService, TokenAuthService}
import com.heimdali.services._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class RiskController[F[_]: Monad](authService: TokenAuthService[F], workspaceService: WorkspaceService[F])
    extends Http4sDsl[F] {

  val route: HttpRoutes[F] =
    authService.tokenRoleAuth(user => user.role == Risk || user.role == Full) {
      AuthedService[User, F] {
        case GET -> Root / "workspaces" as _ =>
          for {
            result <- workspaceService.reviewerList(Risk)
            response <- Ok(result.asJson)
          } yield response
      }
    }

}
