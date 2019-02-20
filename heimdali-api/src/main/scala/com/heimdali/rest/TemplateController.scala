package com.heimdali.rest

import java.time.Clock

import cats.effect._
import com.heimdali.models._
import com.heimdali.services._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class TemplateController(authService: AuthService[IO],
                         templateService: TemplateService[IO])
                        (implicit val clock: Clock) {

  val route: HttpService[IO] =
    authService.tokenAuth {
      AuthedService[User, IO] {
        case GET -> Root  / "user" as user =>
          for {
            userTemplate <- templateService.userDefaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req@POST -> Root  / "user" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, UserTemplate] = jsonOf[IO, UserTemplate]
          for {
            userTemplate <- req.req.as[UserTemplate]
            workspaceRequest <- templateService.userWorkspace(userTemplate)
            response <- Ok(workspaceRequest.copy().asJson)
          } yield response

        case GET -> Root  / "simple" as user =>
          for {
            userTemplate <- templateService.simpleDefaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req@POST -> Root  / "simple" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, SimpleTemplate] = jsonOf[IO, SimpleTemplate]
          for {
            simpleTemplate <- req.req.as[SimpleTemplate]
            workspaceRequest <- templateService.simpleWorkspace(simpleTemplate)
            response <- Ok(workspaceRequest.asJson)
          } yield response

        case GET -> Root  / "structured" as user =>
          for {
            userTemplate <- templateService.structuredDefaults(user)
            response <- Ok(userTemplate.asJson)
          } yield response

        case req@POST -> Root  / "structured" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, StructuredTemplate] = jsonOf[IO, StructuredTemplate]
          for {
            structuredTemplate <- req.req.as[StructuredTemplate]
            workspaceRequest <- templateService.structuredWorkspace(structuredTemplate)
            response <- Ok(workspaceRequest.asJson)
          } yield response
      }
    }

}
