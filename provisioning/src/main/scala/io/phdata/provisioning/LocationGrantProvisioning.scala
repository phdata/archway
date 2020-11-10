package io.phdata.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

object LocationGrantProvisioning {

  implicit val show: Show[LocationGrant] =
    Show.show(g => s"""role "${g.roleName}" access to "${g.location}"""")

  implicit object LocationGrantProvisioningTask extends ProvisioningTask[LocationGrant] {

    override def complete[F[_]: Sync](
        locationGrant: LocationGrant,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.databaseGrantRepository
        .locationGranted(locationGrant.id, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](locationGrant: LocationGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.roleClient.enableAccessToLocation(locationGrant.location, locationGrant.roleName)

  }

  implicit object LocationGrantDeprovisioningTask extends DeprovisioningTask[LocationGrant] {

    override def run[F[_]: Sync: Clock](locationGrant: LocationGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.roleClient.removeAccessToLocation(locationGrant.location, locationGrant.roleName)

  }

  implicit val provisionable: Provisionable[LocationGrant] = Provisionable.deriveFromTasks

}
