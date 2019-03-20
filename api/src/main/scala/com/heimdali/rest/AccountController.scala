package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.models.{Token, User}
import com.heimdali.services.AccountService
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class AccountController[F[_] : Sync](authService: AuthService[F],
                                     accountService: AccountService[F])
  extends Http4sDsl[F] {

  val openRoutes: HttpRoutes[F] =
    authService.basicAuth {
      AuthedService[Token, F] {
        case GET -> Root as token =>
          Ok(token.asJson)
      }
    }

  val tokenizedRoutes: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedService[User, F] {
        case GET -> Root / "profile" as user =>
          Ok(user.asJson)

        case POST -> Root / "workspace" as user =>
          accountService
            .createWorkspace(user)
            .value
            .flatMap {
              case Some(workspace) => Created(workspace.asJson)
              case None => Conflict()
            }

        case GET -> Root / "workspace" as user =>
          for {
            workspace <- accountService.getWorkspace(user.distinguishedName).value
            response <- workspace.fold(NotFound("Not found"))(ws => Ok(ws.asJson))
          } yield response
      }
    }

}
