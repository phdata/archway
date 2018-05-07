package com.heimdali.rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.heimdali.models._
import com.heimdali.services.WorkspaceService
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe._
import io.circe.generic.extras.Configuration
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class WorkspaceController(authService: AuthService,
                          workspaceService: WorkspaceService)
                         (implicit executionContext: ExecutionContext)
  extends ErrorAccumulatingCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withDefaults.withSnakeCaseKeys
  implicit val printer: Printer = Printer.spaces2.copy(dropNullValues = true)
  implicit val timeout: Timeout = Timeout(1 second)

  final def decodeLocalDateTime(formatter: DateTimeFormatter): Decoder[DateTime] =
    Decoder.instance { c =>
      c.as[String] match {
        case Right(s) => try Right(DateTime.parse(s, formatter)) catch {
          case _: Exception => Left(DecodingFailure("DateTime", c.history))
        }
        case l@Left(_) => l.asInstanceOf[Decoder.Result[DateTime]]
      }
    }

  final def encodeLocalDateTime(formatter: DateTimeFormatter): Encoder[DateTime] =
    Encoder.instance(time => Json.fromString(time.toString(formatter)))

  implicit final val decodeLocalDateTimeDefault: Decoder[DateTime] = decodeLocalDateTime(ISODateTimeFormat.basicDateTime())
  implicit final val encodeLocalDateTimeDefault: Encoder[DateTime] = encodeLocalDateTime(ISODateTimeFormat.basicDateTime())

  implicit final val decodeSharedWorkspace: Decoder[SharedWorkspace] =
    (c: HCursor) => {
      for {
        name <- c.downField("name").as[String]
        purpose <- c.downField("purpose").as[String]
        pii <- c.downField("compliance").downField("pii_data").as[Boolean]
        pci <- c.downField("compliance").downField("pci_data").as[Boolean]
        phi <- c.downField("compliance").downField("phi_data").as[Boolean]
        requestedSizeInGB <- c.downField("requested_size_in_gb").as[Int]
        maxCores <- c.downField("requested_cores").as[Int]
        maxMemoryInGB <- c.downField("requested_memory_in_gb").as[Int]
      } yield SharedWorkspace(None, name, SharedWorkspace.generateName(name), purpose, new DateTime(), "", requestedSizeInGB, maxCores, maxMemoryInGB, None, Some(Compliance(None, phi, pci, pii)))
    }

  import io.circe.generic.extras.auto._

  implicit final val encodeCompliance: Encoder[Compliance] =
    Encoder.forProduct3("phi_data", "pci_data", "pii_data")(c => (c.phiData, c.pciData, c.piiData))

  implicit final val encodeHive: Encoder[HiveDatabase] =
    Encoder.forProduct3("name", "location", "role")(c => (c.name, c.location, c.role))

  implicit final val encodeLDAP: Encoder[LDAPRegistration] =
    Encoder.forProduct2("distinguished_name", "common_name")(c => (c.distinguishedName, c.commonName))

  implicit final val encodeSharedWorkspace: Encoder[SharedWorkspace] =
    Encoder.forProduct13("id", "name", "system_name", "purpose", "created", "created_by", "requested_size_in_gb", "requested_cores", "requested_memory_in_gb", "compliance", "ldap", "data", "processing") { u =>
      (u.id, u.name, u.systemName, u.purpose, u.created, u.createdBy, u.requestedSize, u.requestedCores, u.requestedMemory, u.compliance, u.ldap, u.hiveDatabase, u.yarn)
    }

  val route =
    pathPrefix("workspaces") {
      authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
        pathEnd {
          post {
            entity(as[SharedWorkspace]) { workspace =>
              val request = workspace.copy(createdBy = user.username)
              onComplete(workspaceService.create(request)) {
                case Success(newWorkspace: SharedWorkspace) =>
                  complete(StatusCodes.Created -> newWorkspace)
                case Failure(exception) =>
                  complete(StatusCodes.BadRequest -> exception.getMessage)
              }
            }
          } ~
            get {
              onSuccess(workspaceService.list(user.username)) { workspaces =>
                complete(workspaces)
              }
            }
        } ~
          pathPrefix(LongNumber) { id =>
            pathEnd {
              onSuccess(workspaceService.find(id)) {
                case Some(workspace) =>
                  complete(workspace)
                case _ =>
                  complete(StatusCodes.NotFound)
              }
            } ~
            pathPrefix("members") {
              pathEnd {
                get {
                  onSuccess(workspaceService.members(id)) { members =>
                    complete(members)
                  }
                } ~
                post {
                  entity(as[MemberRequest]) { request =>
                    onSuccess(workspaceService.addMember(id, request.username)) { member =>
                      complete(member)
                    }
                  }
                }
              }
            }
          }
      }
    }

}
