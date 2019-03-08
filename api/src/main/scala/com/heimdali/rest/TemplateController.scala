package com.heimdali.rest

import java.time.Clock

import cats.effect._
import com.heimdali.models._
import com.heimdali.templates.WorkspaceGenerator
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._

class TemplateController(authService: AuthService[IO],
                         simpleTemplateGenerator: WorkspaceGenerator[IO, SimpleTemplate],
                         structuredTemplateGenerator: WorkspaceGenerator[IO, StructuredTemplate])
                        (implicit val clock: Clock) {

  val route: HttpService[IO] =
    authService.tokenAuth {
      AuthedService[User, IO] {

        case GET -> Root / "simple" as user =>
          for {
            userTemplate <- simpleTemplateGenerator.defaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req@POST -> Root / "simple" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, SimpleTemplate] = jsonOf[IO, SimpleTemplate]
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
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, StructuredTemplate] = jsonOf[IO, StructuredTemplate]
          for {
            structuredTemplate <- req.req.as[StructuredTemplate]
            workspaceRequest <- structuredTemplateGenerator.workspaceFor(structuredTemplate)
            response <- Ok(workspaceRequest.asJson)
          } yield response
      }
    }

}
