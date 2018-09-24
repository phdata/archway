package com.heimdali.rest

import java.time.Clock

import cats.effect._
import com.heimdali.models._
import com.heimdali.services.Generator._
import com.heimdali.services._
import io.circe.Printer
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class TemplateController(authService: AuthService[IO])
                        (implicit val clock: Clock) {

  val route: HttpService[IO] =
    authService.tokenAuth {
      AuthedService[User, IO] {
        case GET -> Root  / "user" as user =>
          Ok(Generator[UserTemplate].defaults(user).asJson)

        case req@POST -> Root  / "user" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, UserTemplate] = jsonOf[IO, UserTemplate]
          for {
            userTemplate <- req.req.as[UserTemplate]
            workspaceRequest <- IO.pure(userTemplate.generate())
            response <- Ok(workspaceRequest.copy().asJson)
          } yield response

        case GET -> Root  / "simple" as user =>
          Ok(Generator[SimpleTemplate].defaults(user).asJson)

        case req@POST -> Root  / "simple" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, SimpleTemplate] = jsonOf[IO, SimpleTemplate]
          for {
            simpleTemplate <- req.req.as[SimpleTemplate]
            workspaceRequest <- IO.pure(simpleTemplate.generate())
            response <- Ok(workspaceRequest.asJson)
          } yield response

        case GET -> Root  / "structured" as user =>
          Ok(Generator[StructuredTemplate].defaults(user).asJson)

        case req@POST -> Root  / "structured" as _ =>
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, StructuredTemplate] = jsonOf[IO, StructuredTemplate]
          for {
            structuredTemplate <- req.req.as[StructuredTemplate]
            workspaceRequest <- IO.pure(structuredTemplate.generate())
            response <- Ok(workspaceRequest.asJson)
          } yield response
      }
    }

}
