package io.phdata.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._

object DiskQuotaProvisioning {

  implicit val show: Show[DiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit object DiskQuotaProvisioningTask extends ProvisioningTask[DiskQuota] {
    override def run[F[_]: Sync: Clock](diskQuota: DiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] = {
      workspaceContext.context.hdfsService
        .setQuota(diskQuota.location, diskQuota.sizeInGB, workspaceContext.workspaceId, Instant.now())
    }

    override def complete[F[_]: Sync](
        diskQuota: DiskQuota,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] = {
      ().pure[F]
    }
  }

  implicit object DiskQuotaDeprovisioningTask extends DeprovisioningTask[DiskQuota] {

    override def run[F[_]: Sync: Clock](diskQuota: DiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] = {
      workspaceContext.context.hdfsService.removeQuota(diskQuota.location)
    }

  }

  implicit val provisionable: Provisionable[DiskQuota] = Provisionable.deriveFromTasks

}
