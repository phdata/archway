package io.phdata.rest

import cats.Monad
import cats.implicits._
import io.circe.syntax._
import io.phdata.models.User
import io.phdata.rest.authentication.TokenAuthService
import io.phdata.services.MemberService
import org.http4s._
import org.http4s.dsl.Http4sDsl

class MemberController[F[_]: Monad](authService: TokenAuthService[F], memberService: MemberService[F])
    extends Http4sDsl[F] {

  val route: HttpService[F] =
    authService.tokenAuth {
      AuthedService[User, F] {
        case GET -> Root / filter as _ =>
          for {
            members <- memberService.availableMembers(filter)
            response <- Ok(members.asJson)
          } yield response
      }
    }

}
