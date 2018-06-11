package com.heimdali.rest

import cats.effect._
import com.heimdali.models.{Token, User}
import com.heimdali.services.AccountService
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
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


  //    pathPrefix("account") {
  //      path("token") {
  //        get {
  //          authenticateOrRejectWithChallenge(authService.validateCredentials _) {
  //            case Right(user) => complete(user)
  //            case Left(challenge) => reject()
  //          }
  //        }
  //      } ~
  //        path("profile") {
  //          get {
  //            authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
  //              complete(user)
  //            }
  //          }
  //        } ~
  //        path("workspace") {
  //          post {
  //            authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
  //              onSuccess(accountService.createWorkspace(user.username).unsafeToFuture()) { _ =>
  //                complete(StatusCodes.Created)
  //              }
  //            }
  //          } ~
  //            get {
  //              authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
  //                onSuccess(accountService.findWorkspace(user.username).unsafeToFuture()) {
  //                  case Some(workspace) => complete(workspace)
  //                  case None => complete(StatusCodes.NotFound)
  //                }
  //              }
  //            }
  //        }

}
