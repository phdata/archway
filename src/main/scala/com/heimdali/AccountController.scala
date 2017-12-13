package com.heimdali

import akka.http.scaladsl.server.Directives.{authenticateOAuth2Async, authenticateOrRejectWithChallenge, complete, get, path, pathPrefix, reject}

class AccountController {

  val route =
    pathPrefix("account") {
      path("token") {
        get {
          authenticateOrRejectWithChallenge(validateCredentials _) {
            case Right(user) => complete(user)
            case Left(challenge) => reject()
          }
        }
      } ~
        path("profile") {
          get {
            authenticateOAuth2Async("heimdali", authenticator = validateToken) { user =>
              complete(user)
            }
          }
        }
    }

}
