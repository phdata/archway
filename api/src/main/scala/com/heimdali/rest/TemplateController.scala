package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.generators.WorkspaceGenerator
import com.heimdali.models._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class TemplateController[F[_] : Sync](authService: AuthService[F],
                                      simpleTemplateGenerator: WorkspaceGenerator[F, SimpleTemplate],
                                      structuredTemplateGenerator: WorkspaceGenerator[F, StructuredTemplate])
  extends Http4sDsl[F] {

  val route: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedService[User, F] {

        case GET -> Root / "simple" as user =>
          for {
            userTemplate <- simpleTemplateGenerator.defaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req@POST -> Root / "simple" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[F, SimpleTemplate] = jsonOf[F, SimpleTemplate]
          for {
            simpleTemplate <- req.req.as[SimpleTemplate]
            workspaceRequest <- simpleTemplateGenerator.workspaceFor(simpleTemplate)
            response <- Ok(workspaceRequest.asJson)
          } yield response

        case GET -> Root / "structured" as user =>
          for {
            userTemplate <- structuredTemplateGenerator.defaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req@POST -> Root / "structured" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[F, StructuredTemplate] = jsonOf[F, StructuredTemplate]
          for {
            structuredTemplate <- req.req.as[StructuredTemplate]
            workspaceRequest <- structuredTemplateGenerator.workspaceFor(structuredTemplate)
            response <- Ok(workspaceRequest.asJson)
          } yield response
      }
    }

}
