package com.heimdali.provisioning

import cats.Show
import cats.data.{NonEmptyList, WriterT}
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.Application
import com.heimdali.provisioning.Provisionable.ops._

trait ApplicationProvisioning {

  implicit object ApplicationProvisionable extends Provisionable[Application] {
    override def provision[F[_] : Clock : Sync](application: Application, workspaceContext: WorkspaceContext[F])(implicit show: Show[Application]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
      for {
        group <- application.group.provision[F](workspaceContext)
        grant <- GrantRoleToConsumerGroup(application.id.get, application.consumerGroup, application.group.sentryRole).provision[F](workspaceContext)
      } yield group |+| grant
  }

}
