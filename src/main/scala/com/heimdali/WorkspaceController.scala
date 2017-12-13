package com.heimdali

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.heimdali.models.ViewModel.SharedWorkspace
import com.heimdali.services.WorkspaceService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext

class WorkspaceController(authService: AuthService,
                          workspaceService: WorkspaceService)
                         (implicit executionContext: ExecutionContext)
extends FailFastCirceSupport {

  import io.circe.java8.time._
  import io.circe.generic.auto._

  val route =
    path("workspaces") {
      authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
        post {
          entity(as[SharedWorkspace]) { workspace =>
            onSuccess(workspaceService.create(workspace.copy(createdBy = user.username))) { newWorkspace =>
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
