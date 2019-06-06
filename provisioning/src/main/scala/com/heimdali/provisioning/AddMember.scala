package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._

case class AddMember(ldapRegistrationId: Long, groupDN: String, distinguishedName: String)

object AddMember {

  implicit val show: Show[AddMember] =
    Show.show(am => s"""adding "${am.distinguishedName}" to "${am.groupDN}""")

  implicit object AddMemberCompletionTask extends CompletionTask[AddMember] {

    override def apply[F[_] : Sync](addMember: AddMember, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.memberRepository.complete(addMember.ldapRegistrationId, addMember.distinguishedName)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object AddMemberProvisioningTask extends ProvisioningTask[AddMember] {


    override def apply[F[_] : Sync : Clock](addMember: AddMember, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.provisioningLDAPClient.addUser(addMember.groupDN, addMember.distinguishedName)
        .value.void

  }

  implicit val provisionable: Provisionable[AddMember] = Provisionable.deriveProvisionable

}
