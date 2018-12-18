package com.heimdali.rest

import cats.effect._
import com.heimdali.models._
import com.heimdali.services._
import org.http4s._
import org.http4s.dsl.io._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._

class RiskController(authService: AuthService[IO],
                     workspaceService: WorkspaceService[IO]) {

  val route: HttpRoutes[IO] =
    authService.tokenRoleAuth(user => user.role == Risk || user.role == Full) {
      AuthedService[User, IO] {
        case GET -> Root / "workspaces" as _ =>
          for {
            result <- workspaceService.reviewerList(Risk)
            response <- Ok(result.asJson)
          } yield response
      }
    }

}
