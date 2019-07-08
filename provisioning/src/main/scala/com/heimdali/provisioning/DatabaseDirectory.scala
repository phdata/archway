package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._

case class DatabaseDirectory(workspaceId: Long, location: String, onBehalfOf: Option[String])

object DatabaseDirectory {

  implicit val show: Show[DatabaseDirectory] =
    Show.show(c => s"""db directory "${c.location}"""")

  implicit object DatabaseDirectoryProvisioningTask extends ProvisioningTask[DatabaseDirectory] {

    override def complete[F[_]: Sync](
        createDatabaseDirectory: DatabaseDirectory,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.databaseRepository
        .directoryCreated(createDatabaseDirectory.workspaceId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](
        createDatabaseDirectory: DatabaseDirectory,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.hdfsClient.createHiveDirectory(createDatabaseDirectory.location).void

  }

  implicit object DatabaseDirectoryDeprovisioningTask extends DeprovisioningTask[DatabaseDirectory] {

    override def run[F[_]: Sync: Clock](
        createDatabaseDirectory: DatabaseDirectory,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      Sync[F].unit // skip deprovisioning

  }

  implicit val provisionable: Provisionable[DatabaseDirectory] = Provisionable.deriveFromTasks

}
