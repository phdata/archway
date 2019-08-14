package io.phdata.provisioning

import java.time.Instant

import cats.Show
import cats.implicits._
import doobie.implicits._
import cats.effect.{Clock, Sync}

object DiskQuotaProvisioning {

  implicit val show: Show[DiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit object DiskQuotaProvisioningTask extends ProvisioningTask[DiskQuota] {

    override def complete[F[_]: Sync](
        diskQuota: DiskQuota,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.databaseRepository
        .quotaSet(diskQuota.workspaceId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](diskQuota: DiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.hdfsClient.setQuota(diskQuota.location, diskQuota.sizeInGB).void

  }

  implicit object DiskQuotaDeprovisioningTask extends DeprovisioningTask[DiskQuota] {

    override def run[F[_]: Sync: Clock](diskQuota: DiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.hdfsClient.removeQuota(diskQuota.location)

  }

  implicit val provisionable: Provisionable[DiskQuota] = Provisionable.deriveFromTasks

}
