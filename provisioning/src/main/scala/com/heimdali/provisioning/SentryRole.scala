package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect._
import cats.implicits._
import doobie.implicits._

case class SentryRole(id: Long, name: String)

object SentryRole {

  implicit val show: Show[SentryRole] =
    Show.show(c => s"""sentry role "${c.name}"""")

  implicit object SentryRoleProvisioningTask extends ProvisioningTask[SentryRole] {

    override def complete[F[_] : Sync](sentryRole: SentryRole, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.ldapRepository.roleCreated(sentryRole.id, instant)
        .transact(workspaceContext.context.transactor).void

    override def run[F[_] : Sync : Clock](sentryRole: SentryRole, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.createRole(sentryRole.name)

  }

  implicit object SentryRoleDeprovisioningTask extends DeprovisioningTask[SentryRole] {

    override def run[F[_] : Sync : Clock](createRole: SentryRole, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.dropRole(createRole.name)

  }

  implicit val provisionable: Provisionable[SentryRole] = Provisionable.deriveFromTasks

}
