package com.heimdali.provisioning

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.WorkspaceRequest
import com.heimdali.provisioning.Provisionable.ops._
import doobie.implicits._

trait WorkspaceRequestProvisioning {

  implicit object WorkspaceRequestProvisioningTask extends ProvisioningTask[WorkspaceRequest] {

    override def complete[F[_] : Sync](a: WorkspaceRequest, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.workspaceRequestRepository.markProvisioned(workspaceContext.workspaceId, instant)
        .transact(workspaceContext.context.transactor).void

    override def run[F[_] : Sync : Clock](workspace: WorkspaceRequest, workspaceContext: WorkspaceContext[F]): F[Unit] =
      for {
        a <- workspace.data.traverse(a => a.provision[F](workspaceContext).run)
        b <- workspace.data.traverse(d => GroupMember(d.id.get, d.managingGroup.ldapRegistration.distinguishedName, workspace.requestedBy).provision[F](workspaceContext).run)
        c <- workspace.processing.traverse(_.provision[F](workspaceContext).run)
        d <- workspace.applications.traverse(_.provision[F](workspaceContext).run)
        e <- workspace.applications.traverse(d => GroupMember(d.id.get, d.group.distinguishedName, workspace.requestedBy).provision[F](workspaceContext).run)
        f <- workspace.kafkaTopics.traverse(_.provision[F](workspaceContext).run)
        g <- workspace.kafkaTopics.traverse(d => GroupMember(d.id.get, d.managingRole.ldapRegistration.distinguishedName, workspace.requestedBy).provision[F](workspaceContext).run)
      } yield a |+| b |+| c |+| d |+| e |+| f |+| g

  }

  implicit object WorkspaceRequestDeprovisioningTask extends DeprovisioningTask[WorkspaceRequest] {
    override def run[F[_] : Sync : Clock](workspace: WorkspaceRequest, workspaceContext: WorkspaceContext[F]): F[Unit] =
      for {
        g <- workspace.kafkaTopics.traverse(d => GroupMember(d.id.get, d.managingRole.ldapRegistration.distinguishedName, workspace.requestedBy).deprovision[F](workspaceContext).run)
        f <- workspace.kafkaTopics.traverse(_.deprovision[F](workspaceContext).run)
        e <- workspace.applications.traverse(d => GroupMember(d.id.get, d.group.distinguishedName, workspace.requestedBy).deprovision[F](workspaceContext).run)
        d <- workspace.applications.traverse(_.deprovision[F](workspaceContext).run)
        c <- workspace.processing.traverse(_.deprovision[F](workspaceContext).run)
        b <- workspace.data.traverse(d => GroupMember(d.id.get, d.managingGroup.ldapRegistration.distinguishedName, workspace.requestedBy).deprovision[F](workspaceContext).run)
        a <- workspace.data.traverse(a => a.deprovision[F](workspaceContext).run)
      } yield a |+| b |+| c |+| d |+| e |+| f |+| g
  }

  implicit val provisionable: Provisionable[WorkspaceRequest] = Provisionable.deriveFromTasks
}
