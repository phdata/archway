package com.heimdali.generators

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.WorkspaceRequest
import com.heimdali.services.ConfigService

class PioneerLDAPGroupGenerator[F[_] : Monad](configService: ConfigService[F])
                                             (implicit clock: Clock[F], F: Sync[F])
  extends DefaultLDAPGroupGenerator[F](configService) {

  def pioneerAttributes(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[List[(String, String)]] =
    List(
      "managedBy" -> sys.env("HEIMDALI_PIONEER_MANAGED_BY"),
      "description" -> workspace.summary,
      "name" -> cn,
      "source" -> dn
    ).pure[F]

  override def attributes(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[List[(String, String)]] =
    for {
      default <- super.attributes(cn, dn, role, workspace)
      more <- pioneerAttributes(cn, dn, role, workspace)
    } yield default ++ more
}
