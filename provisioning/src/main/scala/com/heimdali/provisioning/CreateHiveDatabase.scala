package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.WorkspaceRequest
import doobie.implicits._

case class CreateHiveDatabase(workspaceId: Long, name: String, location: String)

object CreateHiveDatabase {

  implicit val viewer: Show[CreateHiveDatabase] =
    Show.show(c => s"""creating Hive database "${c.name}" at "${c.location}"""")

  implicit object CreateHiveDatabaseCompletionTask extends CompletionTask[CreateHiveDatabase] {

    override def apply[F[_] : Sync](createHiveDatabase: CreateHiveDatabase, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.databaseRepository.databaseCreated(createHiveDatabase.workspaceId, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object CreateHiveDatabaseProvisioningTask extends ProvisioningTask[CreateHiveDatabase] {

    override def apply[F[_] : Sync : Clock](createHiveDatabase: CreateHiveDatabase, workspaceContext: WorkspaceContext[F]): F[Unit] =
      for {
        workspace <- workspaceContext.context.workspaceRequestRepository.find(workspaceContext.workspaceId).value
          .transact(workspaceContext.context.transactor).map(_.get)
        _ <- workspaceContext.context.hiveClient.createDatabase(createHiveDatabase.name, createHiveDatabase.location, workspace.summary, createDBProperties(workspace))
      } yield ()

  }

  implicit val provisionable: Provisionable[CreateHiveDatabase] = Provisionable.deriveProvisionable

  def createDBProperties(workspaceRequest: WorkspaceRequest): Map[String, String] = {
    val compliance = workspaceRequest.compliance
    Map("phi_data" -> compliance.phiData.toString, "pci_data" -> compliance.phiData.toString, "pii_data" -> compliance.piiData.toString)
  }

}
