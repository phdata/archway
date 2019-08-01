package io.phdata.provisioning

import java.time.Instant

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import io.phdata.models.WorkspaceRequest
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._

case class HiveDatabaseRegistration(workspaceId: Long, name: String, location: String)

object HiveDatabaseRegistration extends LazyLogging {

  implicit val viewer: Show[HiveDatabaseRegistration] =
    Show.show(c => s"""Hive database "${c.name}" at "${c.location}"""")

  implicit object HiveDatabaseRegistrationProvisioningTask extends ProvisioningTask[HiveDatabaseRegistration] {

    override def complete[F[_]: Sync](
        hiveDatabaseRegistration: HiveDatabaseRegistration,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.databaseRepository
        .databaseCreated(hiveDatabaseRegistration.workspaceId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](
        hiveDatabaseRegistration: HiveDatabaseRegistration,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] = {

      for {
        workspace <- workspaceContext.context.workspaceRequestRepository
          .find(workspaceContext.workspaceId)
          .value
          .transact(workspaceContext.context.transactor)
          .map(_.get)
        _ <- workspaceContext.context.hiveClient.createDatabase(hiveDatabaseRegistration.name,
                                                                hiveDatabaseRegistration.location,
                                                                workspace.summary,
                                                                createDBProperties(workspace))
      } yield ()
    }
  }

  implicit object HiveDatabaseRegistrationDeprovisioningTask extends DeprovisioningTask[HiveDatabaseRegistration] {

    override def run[F[_]: Sync: Clock](
        hiveDatabaseRegistration: HiveDatabaseRegistration,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.hiveClient.dropDatabase(hiveDatabaseRegistration.name).void

  }

  implicit val provisionable: Provisionable[HiveDatabaseRegistration] = Provisionable.deriveFromTasks

  def createDBProperties(workspaceRequest: WorkspaceRequest): Map[String, String] = {
    val compliance = workspaceRequest.compliance
    Map(
      "phi_data" -> compliance.phiData.toString,
      "pci_data" -> compliance.phiData.toString,
      "pii_data" -> compliance.piiData.toString
    )
  }

}
