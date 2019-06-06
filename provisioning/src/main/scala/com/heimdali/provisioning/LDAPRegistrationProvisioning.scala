package com.heimdali.provisioning

import cats.Show
import cats.data._
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.LDAPRegistration
import com.heimdali.provisioning.Provisionable.ops._

trait LDAPRegistrationProvisioning {

  implicit object LDAPRegistrationProvisionable extends Provisionable[LDAPRegistration] {

    override def provision[F[_] : Clock : Sync](registration: LDAPRegistration, workspaceContext: WorkspaceContext[F])(implicit show: Show[LDAPRegistration]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
      for {
        group <- CreateLDAPGroup(registration.id.get, registration.commonName, registration.distinguishedName, registration.attributes).provision[F](workspaceContext)
        role <- CreateRole(registration.id.get, registration.sentryRole).provision[F](workspaceContext)
        grant <- GrantGroupAccess(registration.id.get, registration.sentryRole, registration.commonName).provision[F](workspaceContext)
      } yield role |+| group |+| grant
  }

}
