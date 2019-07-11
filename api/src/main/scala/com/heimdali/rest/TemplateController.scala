package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.rest.authentication.TokenAuthService
import com.heimdali.services.TemplateService
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class TemplateController[F[_]: Sync](authService: TokenAuthService[F], templateGenerator: TemplateService[F])
    extends Http4sDsl[F] {

  val route: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedService[User, F] {

        case GET -> Root / custom as _ =>
          for {
            customTemplates <- templateGenerator.customTemplates
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
            simpleTemplate <- req.req.as[TemplateRequest].map(_.copy(requester = user.distinguishedName))
            workspaceRequest <- templateGenerator.workspaceFor(simpleTemplate, templateName)
            response <- Ok(workspaceRequest.asJson)
          } yield response
      }
    }

}
