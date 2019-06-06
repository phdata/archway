package com.heimdali.provisioning

import cats.Show
import cats.data.{NonEmptyList, WriterT}
import cats.effect.{Clock, Sync}
import com.heimdali.models.Yarn
import com.heimdali.provisioning.Provisionable.ops._

trait YarnProvisioning {

  implicit object YarnProvisionable extends Provisionable[Yarn] {

    override def provision[F[_] : Clock : Sync](yarn: Yarn, workspaceContext: WorkspaceContext[F])(implicit show: Show[Yarn]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
      CreateResourcePool(yarn.id.get, yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB).provision[F](workspaceContext)

  }

}
