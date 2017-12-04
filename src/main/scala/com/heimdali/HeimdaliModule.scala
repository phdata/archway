package com.heimdali

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenges, HttpCredentials}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.AuthenticationResult
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.heimdali.models.Project
import com.heimdali.services._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext

class HeimdaliAPI(clusterService: ClusterService,
                  projectService: ProjectService,
                  accountService: AccountService)
                 (implicit val actorSystem: ActorSystem,
                  val executionContext: ExecutionContext,
                  val materializer: Materializer)
  extends FailFastCirceSupport {

  import io.circe.java8.time._
  import io.circe.generic.auto._

  def validateCredentials(creds: Option[HttpCredentials]) =
    creds match {
      case Some(BasicHttpCredentials(username, password)) =>
        accountService.login(username, password).map {
          case Some(user) => Right(user)
          case None => Left(HttpChallenges.basic("heimdali"))
        }
    }

  val route: Route =
    pathPrefix("account") {
      path("token") {
        get {
          authenticateOrRejectWithChallenge(validateCredentials _) { credentials =>
            complete(credentials)
          }
        }
      }
    } ~
      path("clusters") {
        get {
          onSuccess(clusterService.list) { clusters =>
            complete(clusters)
          }
        }
      } ~
      path("workspaces") {
        post {
          entity(as[Project]) { project =>
            onSuccess(projectService.create(project)) { newProject =>
              complete(StatusCodes.Created -> newProject)
            }
          }
        } ~
          get {
            onSuccess(projectService.list("")) { projects =>
              complete(projects)
            }
          }
      }
}