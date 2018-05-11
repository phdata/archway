package com.heimdali.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import cats.kernel.Semigroup
import com.heimdali.models.{HiveDatabase, LDAPRegistration, UserWorkspace, Yarn}
import com.heimdali.provisioning.WorkspaceProvisioner.Start
import com.heimdali.services.AccountService
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.extras.Configuration
import io.circe.{Encoder, Json}

import scala.concurrent.duration._

class AccountController(authService: AuthService,
                        accountService: AccountService,
                        config: Config)
  extends FailFastCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withDefaults.withSnakeCaseKeys
  implicit val timeout: Timeout = Timeout(1 second)

  import io.circe.generic.extras.auto._

  def hive(hive: HiveDatabase) = Json.obj(
    "database" -> Json.obj(
      "name" -> Json.fromString(hive.name),
      "location" -> Json.fromString(hive.location),
      "role" -> Json.fromString(hive.role)
    )
  )

  def ldap(ldap: LDAPRegistration) = Json.obj(
    "ldap" -> Json.obj(
      "common_name" -> Json.fromString(ldap.commonName),
      "distinguished_name" -> Json.fromString(ldap.distinguishedName)
    )
  )

  def yarn(yarn: Yarn) = Json.obj(
    "processing" -> Json.obj(
      "pool_name" -> Json.fromString(yarn.poolName),
      "max_cores" -> Json.fromLong(yarn.maxCores),
      "max_memory" -> Json.fromLong(yarn.maxMemoryInGB)
    )
  )

  implicit final val encodeSharedWorkspace: Encoder[UserWorkspace] =
    (userWorkspace: UserWorkspace) => {
      Seq(
        Some(Json.obj("username" -> Json.fromString(userWorkspace.username))),
        userWorkspace.ldap.map(ldap),
        userWorkspace.hiveDatabase.map(hive),
        userWorkspace.yarn.map(yarn)
      ).flatten
        .reduce(_ deepMerge _)
    }

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
              onSuccess(accountService.createWorkspace(user.username)) { _ =>
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
