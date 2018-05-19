package com.heimdali.rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.heimdali.models._
import com.heimdali.services.GovernedDatasetService
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._
import io.circe.generic.extras.Configuration
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class GovernedDatasetController(authService: AuthService,
                                datasetService: GovernedDatasetService,
                                config: Config)
  extends FailFastCirceSupport {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames
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

  implicit final val decodeGovernedDataset: Decoder[GovernedDataset] =
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
      } yield GovernedDataset(None, name, SharedWorkspace.generateName(name), purpose, new DateTime(), None, requestedSizeInGB, maxCores, maxMemoryInGB, None, Some(Compliance(None, phi, pci, pii)))
    }

  import io.circe.generic.extras.auto._

  implicit final val encodeCompliance: Encoder[Compliance] =
    Encoder.forProduct3("phi_data", "pci_data", "pii_data")(c => (c.phiData, c.pciData, c.piiData))

  implicit final val encodeYarn: Encoder[Yarn] =
    Encoder.forProduct3("pool_name", "max_cores", "max_memory")(c => (c.poolName, c.maxCores, c.maxMemoryInGB))

  implicit final val encodeSharedWorkspace: Encoder[Dataset] =
    Encoder.forProduct6("name", "system_name", "purpose", "ldap", "data", "processing") { u =>
      (u.name, u.systemName, u.purpose, u.ldap, u.hiveDatabase, u.yarn)
    }

  implicit final val encodeGovernedDataset: Encoder[GovernedDataset] =
    Encoder.forProduct13("id", "name", "system_name", "purpose", "created", "created_by", "requested_size_in_gb", "requested_cores", "requested_memory_in_gb", "compliance", "raw", "staging", "modeled") { u =>
      (u.id, u.name, u.systemName, u.purpose, u.created, u.createdBy, u.requestedSize, u.requestedCores, u.requestedMemory, u.compliance, u.rawDataset, u.stagingDataset, u.modeledDataset)
    }

  val route =
    pathPrefix("datasets") {
      authenticateOAuth2Async("heimdali", authService.validateToken) { user =>
        pathEnd {
          post {
            entity(as[GovernedDataset]) { dataset =>
              onComplete(datasetService.create(dataset.copy(createdBy = Some(user.username)))) {
                case Success(newDataset: GovernedDataset) =>
                  complete(StatusCodes.Created -> newDataset)
                case Failure(exception) =>
                  complete(StatusCodes.BadRequest -> exception.getMessage)
              }
            }
          } ~
            get {
              onComplete(datasetService.find(user.username)) {
                case Success(datasets) =>
                  complete(StatusCodes.OK -> datasets)
              }
            }
        } ~
          pathPrefix(LongNumber) { id =>
            pathEnd {
              onComplete(datasetService.get(id)) {
                case Success(dataset) =>
                  complete(StatusCodes.OK -> dataset)
                case _ =>
                  complete(StatusCodes.NotFound)
              }
            } ~
              pathPrefix(Segment / "members") { dataset: String =>
                pathEnd {
                  get {
                    onSuccess(datasetService.members(id, dataset)) { members =>
                      complete(members)
                    }
                  } ~
                    post {
                      entity(as[MemberRequest]) { request =>
                        onSuccess(datasetService.addMember(id, dataset, request.username)) { member =>
                          complete(member)
                        }
                      }
                    }
                } ~
                  path(Remaining) { username =>
                    delete {
                      onSuccess(datasetService.removeMember(id, dataset, username)) { member =>
                        complete(member)
                      }
                    }
                  }
              }
          }
      }
    }
}