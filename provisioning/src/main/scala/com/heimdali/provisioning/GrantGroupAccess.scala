package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class GrantGroupAccess(ldapId: Long, roleName: String, groupName: String)

object GrantGroupAccess {

  implicit val show: Show[GrantGroupAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to group ${g.groupName}")

  implicit object GrantGroupAccessCompletionTask extends CompletionTask[GrantGroupAccess] {


    override def apply[F[_] : Sync](grantGroupAccess: GrantGroupAccess, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.ldapRepository.groupAssociated(grantGroupAccess.ldapId, instant)
        .transact(workspaceContext.context.transactor).void
  }

  implicit object GrantGroupAccessProvisioningTask extends ProvisioningTask[GrantGroupAccess] {

    override def apply[F[_] : Sync : Clock](grantGroupAccess: GrantGroupAccess, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.grantGroup(grantGroupAccess.groupName, grantGroupAccess.roleName)

  }

  implicit val provisionable: Provisionable[GrantGroupAccess] = Provisionable.deriveProvisionable

}
