package com.heimdali

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{AuthenticationResult, Credentials, SecurityDirectives}
import akka.stream.Materializer
import com.heimdali.models.ViewModel._
import com.heimdali.services._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.extras.Configuration
import io.circe.{Decoder, HCursor}

import scala.concurrent.{ExecutionContext, Future}

class HeimdaliAPI(clusterService: ClusterService,
                  workspaceService: WorkspaceService,
                  accountService: AccountService)
                 (implicit val actorSystem: ActorSystem,
                  val executionContext: ExecutionContext,
                  val materializer: Materializer)
  extends FailFastCirceSupport {


  import io.circe.java8.time._
  import io.circe.generic.auto._

  implicit val decodeProject: Decoder[SharedWorkspace] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    purpose <- c.downField("purpose").as[String]
    compliance <- c.downField("compliance").as[Compliance]
    hdfs <- c.downField("hdfs").as[HDFSProvision]
  } yield {
    SharedWorkspace(123, name, purpose, None, "", compliance, hdfs, None, LocalDateTime.now(), "")
  }

  def validateCredentials(creds: Option[HttpCredentials]): Future[Either[HttpChallenge, SecurityDirectives.AuthenticationResult[Token]]] =
    creds match {
      case Some(BasicHttpCredentials(username, password)) =>
        accountService.login(username, password).map {
          case Some(user) => Right(AuthenticationResult.success(user))
          case None => Left(HttpChallenges.basic("heimdali"))
        }
    }

  def validateToken(creds: Credentials): Future[Option[User]] =
    creds match {
      case Credentials.Provided(token) =>
        accountService.validate(token)
      case _ =>
        Future(None)
    }

  val route: Route =
     ~
      path("clusters") {
        get {
          onSuccess(clusterService.list) { clusters =>
            complete(clusters)
          }
        }
      } ~
      path("workspaces") {
        post {
          authenticateOAuth2Async("heimdali", authenticator = validateToken) { user =>
            entity(as[SharedWorkspace]) { workspace =>
              onSuccess(workspaceService.create(workspace.copy(createdBy = user.username))) { newWorkspace =>
                complete(StatusCodes.Created -> newWorkspace)
              }
            }
          } ~
            get {
              authenticateOAuth2Async("heimdali", authenticator = validateToken) { user =>
                onSuccess(workspaceService.list(user.username)) { workspaces =>
                  complete(workspaces)
                }
              }
            }
        }
      }
}