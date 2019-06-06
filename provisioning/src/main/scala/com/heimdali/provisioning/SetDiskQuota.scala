package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class SetDiskQuota(workspaceId: Long, location: String, sizeInGB: Int)

object SetDiskQuota {

  implicit val show: Show[SetDiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit object SetDiskQuotaCompletionTask extends CompletionTask[SetDiskQuota] {

    override def apply[F[_] : Sync](setDiskQuota: SetDiskQuota, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.databaseRepository.quotaSet(setDiskQuota.workspaceId, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object SetDiskQuotaProvisioningTask extends ProvisioningTask[SetDiskQuota] {

    override def apply[F[_] : Sync : Clock](setDiskQuota: SetDiskQuota, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.hdfsClient.setQuota(setDiskQuota.location, setDiskQuota.sizeInGB).void

  }

  implicit val provisionable: Provisionable[SetDiskQuota] = Provisionable.deriveProvisionable

}
