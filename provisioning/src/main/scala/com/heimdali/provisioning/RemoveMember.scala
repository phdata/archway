package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._

case class RemoveMember(groupDN: String, userDN: String)

object RemoveMember {

  implicit val show: Show[RemoveMember] =
    Show.show(r => s"""removing "${r.userDN}" from "${r.groupDN}""")

  implicit object RemoveMemberCompletionTask extends CompletionTask[RemoveMember] {

    override def apply[F[_] : Sync](removeMember: RemoveMember, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      Sync[F].unit

  }

  implicit object RemoveMemberProvisioningTask extends ProvisioningTask[RemoveMember] {

    override def apply[F[_] : Sync : Clock](removeMember: RemoveMember, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.provisioningLDAPClient.removeUser(removeMember.groupDN, removeMember.userDN).value.void

  }

  implicit val provisionable: Provisionable[RemoveMember] = Provisionable.deriveProvisionable

}
