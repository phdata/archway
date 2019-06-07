package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class ActiveDirectoryGroup(groupId: Long, commonName: String, distinguishedName: String, attributes: List[(String, String)])

object ActiveDirectoryGroup {

  implicit val show: Show[ActiveDirectoryGroup] =
    Show.show(c => s""""${c.commonName}" group ("${c.distinguishedName}")""")

  implicit object ActiveDirectoryGroupProvisioningTask extends ProvisioningTask[ActiveDirectoryGroup] {

    override def complete[F[_] : Sync](createLDAPGroup: ActiveDirectoryGroup, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.ldapRepository.groupCreated(createLDAPGroup.groupId, instant)
        .transact(workspaceContext.context.transactor).void

    override def run[F[_] : Sync : Clock](createLDAPGroup: ActiveDirectoryGroup, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.provisioningLDAPClient.createGroup(createLDAPGroup.commonName, createLDAPGroup.attributes)

  }

  implicit object ActiveDirectoryGroupDeprovisioningTask extends DeprovisioningTask[ActiveDirectoryGroup] {

    override def run[F[_] : Sync : Clock](createLDAPGroup: ActiveDirectoryGroup, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.provisioningLDAPClient.deleteGroup(createLDAPGroup.commonName).value.void

  }

  implicit val provisionable: Provisionable[ActiveDirectoryGroup] = Provisionable.deriveFromTasks

}
