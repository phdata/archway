package io.phdata.rest

import java.net.URLDecoder

import cats.effect._
import cats.implicits._
import io.phdata.models._
import io.phdata.services.TemplateService
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import io.phdata.models.{TemplateRequest, User, UserDN}
import io.phdata.rest.authentication.TokenAuthService
import io.phdata.services.TemplateService
import org.http4s._
import org.http4s.dsl.Http4sDsl

class TemplateController[F[_]: Sync](authService: TokenAuthService[F], templateGenerator: TemplateService[F])
    extends Http4sDsl[F] with LazyLogging {

  val route: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedService[User, F] {

        case GET -> Root / "custom" as _ =>
          for {
            customTemplates <- templateGenerator.customTemplates.onError {
              case e: Throwable => logger.error(s"Failed to read custom templates: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(customTemplates.map(_.metadata).asJson)
          } yield response

        case GET -> Root / _ as user =>
          for {
            userTemplate <- templateGenerator.defaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req @ POST -> Root / templateName as user =>
          implicit val templateEntityDecoder: EntityDecoder[F, TemplateRequest] = jsonOf[F, TemplateRequest]
          for {
            simpleTemplate <- req.req.as[TemplateRequest].map(_.copy(requester = UserDN(user.distinguishedName)))
            workspaceRequest <- templateGenerator
              .workspaceFor(simpleTemplate, URLDecoder.decode(templateName, "UTF-8"))
              .onError {
                case e: Throwable =>
                  logger
                    .error(
                      s"Error parsing template request for user '${user.username}' using template '$templateName': ${e.getLocalizedMessage}")
                    .pure[F]
              }
            response <- Ok(workspaceRequest.asJson)
          } yield response
      }
    }

}
