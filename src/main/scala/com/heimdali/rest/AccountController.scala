package com.heimdali.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import com.heimdali.actors.user.UserProvisioner.Request
import com.heimdali.services.{AccountService, User}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.extras.Configuration

import scala.concurrent.duration._

class AccountController(authService: AuthService,
                        accountService: AccountService,
                        userProvisionerFactory: User => ActorRef)
  extends FailFastCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withDefaults.withSnakeCaseKeys

  import io.circe.generic.extras.auto._

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
        } ~
        path("workspace") {
          post {
            authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
              onSuccess(userProvisionerFactory(user).ask(Request)(Timeout(1 second))) { _ =>
                complete(StatusCodes.Created)
              }
            }
          } ~
            get {
              authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
                onSuccess(accountService.findWorkspace(user.username)) {
                  case Some(workspace) => complete(workspace)
                  case None => complete(StatusCodes.NotFound)
                }
              }
            }
        }
    }

}
