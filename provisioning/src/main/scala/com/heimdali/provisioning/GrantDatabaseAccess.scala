package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.DatabaseRole
import doobie.implicits._

case class GrantDatabaseAccess(id: Long, roleName: String, databaseName: String, databaseRole: DatabaseRole)

object GrantDatabaseAccess {

  implicit val show: Show[GrantDatabaseAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to ${g.databaseName}")

  implicit object GrantDatabaseAccessCompletionTask extends CompletionTask[GrantDatabaseAccess] {

    override def apply[F[_] : Sync](grantDatabaseAccess: GrantDatabaseAccess, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.databaseGrantRepository.databaseGranted(grantDatabaseAccess.id, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object GrantDatabaseAccessProvisioningTask extends ProvisioningTask[GrantDatabaseAccess] {

    override def apply[F[_] : Sync : Clock](grantDatabaseAccess: GrantDatabaseAccess, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext
        .context
        .sentryClient
        .enableAccessToDB(grantDatabaseAccess.databaseName, grantDatabaseAccess.roleName, grantDatabaseAccess.databaseRole)

  }

  implicit val provisionable: Provisionable[GrantDatabaseAccess] = Provisionable.deriveProvisionable

}
