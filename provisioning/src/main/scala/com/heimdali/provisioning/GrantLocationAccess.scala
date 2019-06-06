package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class GrantLocationAccess(id: Long, roleName: String, location: String)

object GrantLocationAccess {

  implicit val show: Show[GrantLocationAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to location ${g.location}")

  implicit object GrantLocationAccessCompletionTask extends CompletionTask[GrantLocationAccess] {


    override def apply[F[_] : Sync](grantLocationAccess: GrantLocationAccess, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.databaseGrantRepository.locationGranted(grantLocationAccess.id, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object GrantLocationAccessProvisioningTask extends ProvisioningTask[GrantLocationAccess] {

    override def apply[F[_] : Sync : Clock](grantLocationAccess: GrantLocationAccess, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.enableAccessToLocation(grantLocationAccess.location, grantLocationAccess.roleName)

  }

  implicit val provisionable: Provisionable[GrantLocationAccess] = Provisionable.deriveProvisionable

}
