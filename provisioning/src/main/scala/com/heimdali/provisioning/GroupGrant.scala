package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class GroupGrant(ldapId: Long, roleName: String, groupName: String)

object GroupGrant {

  implicit val show: Show[GroupGrant] =
    Show.show(g => s""""${g.roleName}" rights to AD group "${g.groupName}"""")

  implicit object GrantGroupAccessProvisioningTask extends ProvisioningTask[GroupGrant] {

    override def complete[F[_]: Sync](
        grantGroupAccess: GroupGrant,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.ldapRepository
        .groupAssociated(grantGroupAccess.ldapId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](grantGroupAccess: GroupGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.grantGroup(grantGroupAccess.groupName, grantGroupAccess.roleName)

  }

  implicit object GrantGroupAccessDeprovisioningTask extends DeprovisioningTask[GroupGrant] {

    override def run[F[_]: Sync: Clock](grantGroupAccess: GroupGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.revokeGroup(grantGroupAccess.groupName, grantGroupAccess.roleName)

  }

  implicit val provisionable: Provisionable[GroupGrant] = Provisionable.deriveFromTasks

}
