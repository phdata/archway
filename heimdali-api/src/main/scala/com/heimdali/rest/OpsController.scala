package com.heimdali.rest

import cats.effect._
import com.heimdali.models._
import com.heimdali.services._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class OpsController(authService: AuthService[IO],
                    workspaceService: WorkspaceService[IO]) {

  val route: HttpRoutes[IO] =
    authService.tokenRoleAuth(user => user.role == Infra || user.role == Full) {
      AuthedService[User, IO] {
        case GET -> Root / "workspaces" as _ =>
          for {
            result <- workspaceService.reviewerList(Infra)
            response <- Ok(result.asJson)
          } yield response
      }
    }

}
