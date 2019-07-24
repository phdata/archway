package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.{Token, User}
import com.heimdali.rest.authentication.{AuthService, TokenAuthService}
import com.heimdali.services.AccountService
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class AccountController[F[_]: Sync](
    authService: AuthService[F],
    tokenAuthService: TokenAuthService[F],
    accountService: AccountService[F],
    appContext: AppContext[F]
) extends Http4sDsl[F] {

  val clientAuthRoutes: HttpRoutes[F] =
    authService.clientAuth {
      AuthedService[Token, F] {
        case GET -> Root as token =>
          Ok(token.asJson)
      }
    }

  val noAuthRoutes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(Map("authType" -> appContext.appConfig.rest.authType).asJson)
    }
  }

  val tokenizedRoutes: HttpRoutes[F] =
    tokenAuthService.tokenAuth {
      AuthedService[User, F] {
        case GET -> Root / "profile" as user =>
          Ok(user.asJson)

        case POST -> Root / "workspace" as user =>
          accountService
            .createWorkspace(user)
            .value
            .onError {
              case e: Throwable =>
                logger.error("Error creating personal workspace", e).pure[F]
            }
            .flatMap {
              case Some(workspace) => Created(workspace.asJson)
              case None            => Conflict()
            }

        case GET -> Root / "workspace" as user =>
          for {
            workspace <- accountService.getWorkspace(user.distinguishedName).value
            response <- workspace.fold(NotFound("Not found"))(ws => Ok(ws.asJson))
          } yield response

        case GET -> Root / "feature-flags" as _ =>
          Ok(appContext.featureService.all().asJson)
      }
    }

}
