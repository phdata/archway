package io.phdata.provisioning

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import io.phdata.models.WorkspaceRequest
import doobie.implicits._
import io.phdata.provisioning.GroupMemberProvisioning.show

trait WorkspaceRequestProvisioning {

  implicit object WorkspaceRequestProvisioningTask extends ProvisioningTask[WorkspaceRequest] {

    override def complete[F[_]: Sync](
        a: WorkspaceRequest,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.workspaceRequestRepository
        .markProvisioned(workspaceContext.workspaceId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](workspace: WorkspaceRequest, workspaceContext: WorkspaceContext[F]): F[Unit] = {
      (for {
        a <- workspace.data.traverse(a => HiveAllocationProvisionable.provision(a, workspaceContext).run)
        b <- workspace.data.traverse(
          d =>
            GroupMemberProvisioning.provisionable
              .provision(
                GroupMember(
                  d.id.get,
                  d.managingGroup.ldapRegistration.distinguishedName,
                  workspace.requestedBy
                ),
                workspaceContext
              )
              .run
        )
      } yield a |+| b)
    }

  }

  implicit object WorkspaceRequestDeprovisioningTask extends DeprovisioningTask[WorkspaceRequest] {
    override def run[F[_]: Sync: Clock](workspace: WorkspaceRequest, workspaceContext: WorkspaceContext[F]): F[Unit] =
      for {

        b <- workspace.data.traverse(
          d =>
            GroupMemberProvisioning.provisionable
              .deprovision(
                GroupMember(d.id.get, d.managingGroup.ldapRegistration.distinguishedName, workspace.requestedBy),
                workspaceContext
              )
              .run
        )
        a <- workspace.data.traverse(a => HiveAllocationProvisionable.deprovision(a, workspaceContext).run)
        _ <- workspaceContext.context.workspaceRequestRepository
          .markUnprovisioned(workspaceContext.workspaceId)
          .transact(workspaceContext.context.transactor)
          .void
      } yield a |+| b
  }

  implicit val workspaceRequestProvisionable: Provisionable[WorkspaceRequest] =
    Provisionable.deriveFromTasks(WorkspaceRequestProvisioningTask, WorkspaceRequestDeprovisioningTask)
}
