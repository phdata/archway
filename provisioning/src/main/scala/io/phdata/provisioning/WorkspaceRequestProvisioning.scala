package io.phdata.provisioning

import java.time.Instant

import cats.data.{NonEmptyList, OptionT}
import cats.effect.{Clock, Sync}
import cats.implicits._
import io.phdata.config.AvailableFeatures
import io.phdata.models.WorkspaceRequest
import doobie.implicits._
import io.phdata.services.ImpalaServiceImpl
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
      val createUserWorkspace =
        (for {
          user <- workspaceContext.context.lookupLDAPClient.findUserByDN(workspace.requestedBy)
          _ <- OptionT.liftF(workspaceContext.context.hdfsClient.createUserDirectory(user.username))
        } yield ()).value

      createUserWorkspace *>
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
          d <- workspace.applications.traverse(x => ApplicationProvisionable.provision(x, workspaceContext).run)
          e <- workspace.applications.traverse(
            d =>
              GroupMemberProvisioning.provisionable
                .provision(
                  GroupMember(d.id.get, d.group.distinguishedName, workspace.requestedBy),
                  workspaceContext
                )
                .run
          )

          _ <- ImpalaServiceImpl.invalidateMetadata(workspace.id.get)(workspaceContext.context)
        } yield a |+| b |+| d |+| e)
    }

  }

  implicit object WorkspaceRequestDeprovisioningTask extends DeprovisioningTask[WorkspaceRequest] {
    override def run[F[_]: Sync: Clock](workspace: WorkspaceRequest, workspaceContext: WorkspaceContext[F]): F[Unit] =
      for {
        e <- workspace.applications.traverse(
          d =>
            GroupMemberProvisioning.provisionable
              .deprovision(
                GroupMember(d.id.get, d.group.distinguishedName, workspace.requestedBy),
                workspaceContext
              )
              .run
        )
        d <- workspace.applications.traverse(x => ApplicationProvisionable.deprovision(x, workspaceContext).run)
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
      } yield a |+| b |+| d |+| e
  }

  implicit val workspaceRequestProvisionable: Provisionable[WorkspaceRequest] =
    Provisionable.deriveFromTasks(WorkspaceRequestProvisioningTask, WorkspaceRequestDeprovisioningTask)
}
