package com.heimdali.rest

import java.time.LocalDateTime

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.heimdali.models.ViewModel.SharedWorkspaceRequest
import com.heimdali.services.WorkspaceService
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}

import scala.concurrent.ExecutionContext

class WorkspaceController(authService: AuthService,
                          workspaceService: WorkspaceService)
                         (implicit executionContext: ExecutionContext)
extends ErrorAccumulatingCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withDefaults.withSnakeCaseKeys
  implicit val timeEncoder: Encoder[LocalDateTime] = io.circe.java8.time.encodeLocalDateTimeDefault
  implicit val timeDecoder: Decoder[LocalDateTime] = io.circe.java8.time.decodeLocalDateTimeDefault
  import io.circe.generic.extras.auto._

  val route =
    path("workspaces") {
      authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
        post {
          entity(as[SharedWorkspaceRequest]) { workspace =>
            onSuccess(workspaceService.create(workspace.copy(createdBy = Some(user.username)))) { newWorkspace =>
              complete(StatusCodes.Created -> newWorkspace)
            }
          }
        } ~
          get {
            onSuccess(workspaceService.list(user.username)) { workspaces =>
              complete(workspaces)
            }
          }
      }
    }

}
