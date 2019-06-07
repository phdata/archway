package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class DiskQuota(workspaceId: Long, location: String, sizeInGB: Int)

object DiskQuota {

  implicit val show: Show[DiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit object DiskQuotaProvisioningTask extends ProvisioningTask[DiskQuota] {

    override def complete[F[_] : Sync](diskQuota: DiskQuota, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.databaseRepository.quotaSet(diskQuota.workspaceId, instant)
        .transact(workspaceContext.context.transactor).void

    override def run[F[_] : Sync : Clock](diskQuota: DiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.hdfsClient.setQuota(diskQuota.location, diskQuota.sizeInGB).void

  }

  implicit object DiskQuotaDeprovisioningTask extends DeprovisioningTask[DiskQuota] {

    override def run[F[_] : Sync : Clock](diskQuota: DiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.hdfsClient.removeQuota(diskQuota.location)

  }

  implicit val provisionable: Provisionable[DiskQuota] = Provisionable.deriveFromTasks

}
