package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._

case class CreateDatabaseDirectory(workspaceId: Long, location: String, onBehalfOf: Option[String])

object CreateDatabaseDirectory {

  implicit val show: Show[CreateDatabaseDirectory] =
    Show.show(c => s"creating db directory ${c.location}")

  implicit object CreateDatabaseDirectoryCompletionTask extends CompletionTask[CreateDatabaseDirectory] {

    override def apply[F[_] : Sync](createDatabaseDirectory: CreateDatabaseDirectory, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.databaseRepository.directoryCreated(createDatabaseDirectory.workspaceId, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object CreateDatabaseDirectoryProvisioningTask extends ProvisioningTask[CreateDatabaseDirectory] {

    override def apply[F[_] : Sync : Clock](createDatabaseDirectory: CreateDatabaseDirectory, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.hdfsClient.createDirectory(createDatabaseDirectory.location, createDatabaseDirectory.onBehalfOf).void

  }

  implicit val provisionable: Provisionable[CreateDatabaseDirectory] = Provisionable.deriveProvisionable

}
