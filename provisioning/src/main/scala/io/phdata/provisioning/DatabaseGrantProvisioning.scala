package io.phdata.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

object DatabaseGrantProvisioning {

  implicit val show: Show[DatabaseGrant] =
    Show.show(g => s""""${g.databaseRole}" grant for role "${g.roleName}" on "${g.databaseName}"""")

  implicit object DatabaseGrantProvisioningTask extends ProvisioningTask[DatabaseGrant] {

    override def complete[F[_]: Sync](
        databaseGrant: DatabaseGrant,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.databaseGrantRepository
        .databaseGranted(databaseGrant.id, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](databaseGrant: DatabaseGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.roleClient
        .enableAccessToDB(databaseGrant.databaseName, databaseGrant.roleName, databaseGrant.databaseRole)

  }

  implicit object DatabaseGrantDeprovisioningTask extends DeprovisioningTask[DatabaseGrant] {

    override def run[F[_]: Sync: Clock](databaseGrant: DatabaseGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.roleClient
        .removeAccessToDB(databaseGrant.databaseName, databaseGrant.roleName, databaseGrant.databaseRole)

  }

  implicit val databaseGrantProvisionable: Provisionable[DatabaseGrant] =
    Provisionable.deriveFromTasks(DatabaseGrantProvisioningTask, DatabaseGrantDeprovisioningTask)

}
