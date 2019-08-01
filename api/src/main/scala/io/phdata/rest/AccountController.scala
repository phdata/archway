package io.phdata.rest

import cats.effect._
import cats.implicits._
import io.phdata.models.{Token, User}
import io.phdata.rest.authentication.{AuthService, TokenAuthService}
import io.phdata.services.{AccountService, DefaultFileReader}
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import io.phdata.AppContext
import io.phdata.models.{Token, User}
import io.phdata.rest.authentication.{AuthService, TokenAuthService}
import io.phdata.services.{AccountService, DefaultFileReader}
import org.http4s._
import org.http4s.dsl.Http4sDsl

class AccountController[F[_]: Sync](
    authService: AuthService[F],
    tokenAuthService: TokenAuthService[F],
    accountService: AccountService[F],
    appContext: AppContext[F]
) extends Http4sDsl[F] with LazyLogging {

  private val heimdaliVersion: F[String] = {
    sys.env.get("HEIMDALI_DIST") match {
      case Some(value) =>
        for {
          fileLines <- new DefaultFileReader[F].readLines(s"$value/version.txt")
          _ <- Sync[F].pure(logger.info(s"Heimdali version ${fileLines.head}"))
        } yield fileLines.head
      case None =>
        logger.warn("System property HEIMDALI_DIST is not set, unable to retrieve version")
        "".pure[F]
    }
  }

  val clientAuthRoutes: HttpRoutes[F] =
    authService.clientAuth {
      AuthedService[Token, F] {
        case GET -> Root as token =>
          Ok(token.asJson)
        case GET -> Root / "version" as _ =>
          for {
            version <- heimdaliVersion
            response <- Ok(Map("version" -> version).asJson)
          } yield response
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
