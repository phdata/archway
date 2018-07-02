package com.heimdali.rest

import cats.effect._
import com.heimdali.models.{Token, User}
import com.heimdali.services.AccountService
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.io._

class AccountController(authService: AuthService[IO],
                        accountService: AccountService[IO]) {

  val openRoutes: HttpService[IO] =
    authService.basicAuth {
      AuthedService[Token, IO] {
        case GET -> Root as token =>
          Ok(token.asJson)
      }
    }

  val tokenizedRoutes: HttpService[IO] =
    authService.tokenAuth {
      AuthedService[User, IO] {
        case GET -> Root / "profile" as user =>
          Ok(user.asJson)
      }
    }

}