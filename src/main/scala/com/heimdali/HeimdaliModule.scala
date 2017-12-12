package com.heimdali

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives.{AuthenticationResult, _}
import akka.http.scaladsl.server.directives.{AuthenticationDirective, AuthenticationResult, Credentials, SecurityDirectives}
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.heimdali.models.{Compliance, HDFSProvision, Project}
import com.heimdali.services._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.concurrent.{ExecutionContext, Future}

class HeimdaliAPI(clusterService: ClusterService,
                  projectService: ProjectService,
                  accountService: AccountService)
                 (implicit val actorSystem: ActorSystem,
                  val executionContext: ExecutionContext,
                  val materializer: Materializer)
  extends FailFastCirceSupport {

  import io.circe.java8.time._
  import io.circe.generic.auto._

  def decodeProject(username: String): Decoder[Project] = (c: HCursor) => for {
    name <- c.downField("name").as[String]
    purpose <- c.downField("purpose").as[String]
    compliance <- c.downField("compliance").as[Compliance]
    hdfs <- c.downField("hdfs").as[HDFSProvision]
  } yield {
    Project(name, purpose, compliance, hdfs, username)
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
    pathPrefix("account") {
      path("token") {
        get {
          authenticateOrRejectWithChallenge(validateCredentials _) {
            case Right(user) => complete(user)
            case Left(challenge) => reject()
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
          authenticateOAuth2Async("heimdali", authenticator = validateToken) { user =>
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
}