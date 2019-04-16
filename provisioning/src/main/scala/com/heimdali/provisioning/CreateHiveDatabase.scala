package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.effect.{Effect, Timer}
import cats.implicits._
import com.heimdali.models.WorkspaceRequest
import doobie.implicits._

import scala.concurrent.duration._

case class CreateHiveDatabase(workspaceId: Long, name: String, location: String)

object CreateHiveDatabase {

  implicit val viewer: Show[CreateHiveDatabase] =
    Show.show(c => s"""creating Hive database "${c.name}" at "${c.location}"""")
  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, CreateHiveDatabase] =
    ProvisionTask.instance { create =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        val result = for {
          workspace <- context.workspaceRequestRepository.find(id.get).value.transact(context.transactor)
          attempt <- context.hiveClient.createDatabase(create.name, create.location, workspace.get.summary, createDBProperties(workspace.get)).attempt
        } yield attempt

        result.flatMap {
          case Left(exception) => Effect[F].pure[ProvisionResult](Error(id, create, exception))
          case Right(_) =>
            for {
              time <- Timer[F].clock.realTime(MILLISECONDS)
              _ <- context
                .databaseRepository
                .databaseCreated(create.workspaceId, Instant.ofEpochMilli(time))
                .transact(context.transactor)
            } yield Success(id, create)
        }
      }
    }

  def createDBProperties(workspaceRequest: WorkspaceRequest): Map[String, String] = {
    val compliance = workspaceRequest.compliance
    Map("phi_data" -> compliance.phiData.toString, "pci_data" -> compliance.phiData.toString, "pii_data" -> compliance.piiData.toString)
  }

}
