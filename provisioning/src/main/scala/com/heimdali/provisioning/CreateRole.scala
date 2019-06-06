package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.Transactor

case class CreateRole(id: Long, name: String)

object CreateRole {

  implicit val show: Show[CreateRole] =
    Show.show(c => s"creating sentry role ${c.name}")

  implicit object CreateRoleCompletionTask extends CompletionTask[CreateRole] {

    override def apply[F[_] : Sync](createRole: CreateRole, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.ldapRepository.roleCreated(createRole.id, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object CreateRoleProvisioningTask extends ProvisioningTask[CreateRole] {

    override def apply[F[_] : Sync : Clock](createRole: CreateRole, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.sentryClient.createRole(createRole.name)

  }

  implicit val provisionable: Provisionable[CreateRole] = Provisionable.deriveProvisionable

}
