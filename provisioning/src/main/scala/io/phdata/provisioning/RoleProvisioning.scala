package io.phdata.provisioning

import java.time.Instant

import cats.Show
import cats.effect._
import cats.implicits._
import doobie.implicits._

object RoleProvisioning {

  implicit val show: Show[SecurityRole] =
    Show.show(c => s"""sentry role "${c.name}"""")

  implicit object RoleProvisioningTask extends ProvisioningTask[SecurityRole] {

    override def complete[F[_]: Sync](
        securityRole: SecurityRole,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.ldapRepository
        .roleCreated(securityRole.id, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](securityRole: SecurityRole, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.roleClient.createRole(securityRole.name)

  }

  implicit object RoleDeprovisioningTask extends DeprovisioningTask[SecurityRole] {

    override def run[F[_]: Sync: Clock](createRole: SecurityRole, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.roleClient.dropRole(createRole.name)

  }

  implicit val provisionable: Provisionable[SecurityRole] =
    Provisionable.deriveFromTasks(RoleProvisioningTask, RoleDeprovisioningTask)

}
