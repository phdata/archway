package com.heimdali

import akka.http.scaladsl.server.Directives.{authenticateOAuth2Async, authenticateOrRejectWithChallenge, complete, get, path, pathPrefix, reject, _}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

class AccountController(authService: AuthService)
  extends FailFastCirceSupport {

  import io.circe.java8.time._
  import io.circe.generic.auto._

  val route =
    pathPrefix("account") {
      path("token") {
        get {
          authenticateOrRejectWithChallenge(authService.validateCredentials _) {
            case Right(user) => complete(user)
            case Left(challenge) => reject()
          }
        }
      } ~
        path("profile") {
          get {
            authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
              complete(user)
            }
          }
        }
    }

}
