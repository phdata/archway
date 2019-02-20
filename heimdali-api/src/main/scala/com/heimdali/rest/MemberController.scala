package com.heimdali.rest

import cats.effect._
import com.heimdali.models.User
import com.heimdali.services.MemberService
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class MemberController(authService: AuthService[IO],
                       memberService: MemberService[IO]) {

  val route: HttpService[IO] =
    authService.tokenAuth {
      AuthedService[User, IO] {
        case GET -> Root / filter as _ =>
          for {
            members <- memberService.availableMembers(filter)
            response <- Ok(members.asJson)
          } yield response
      }
    }

}
