package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class CreateLDAPGroup(groupId: Long, commonName: String, distinguishedName: String, attributes: List[(String, String)])

object CreateLDAPGroup {

  implicit val show: Show[CreateLDAPGroup] =
    Show.show(c => s"creating ${c.commonName} group using gid ${c.groupId} at ${c.distinguishedName}}")


  implicit object CreateLDAPGroupCompletionTask extends CompletionTask[CreateLDAPGroup] {

    override def apply[F[_] : Sync](createLDAPGroup: CreateLDAPGroup, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.ldapRepository.groupCreated(createLDAPGroup.groupId, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object CreateLDAPGroupProvisioningTask extends ProvisioningTask[CreateLDAPGroup] {

    override def apply[F[_] : Sync : Clock](createLDAPGroup: CreateLDAPGroup, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.provisioningLDAPClient.createGroup(createLDAPGroup.commonName, createLDAPGroup.attributes)

  }

  implicit val provisionable: Provisionable[CreateLDAPGroup] = Provisionable.deriveProvisionable

}
