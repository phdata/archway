package com.heimdali.generators

import java.time.Clock

import cats._
import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.WorkspaceRequest

class PioneerLDAPGroupGenerator[F[_] : Monad](appConfig: AppConfig)
                                             (implicit clock: Clock, F: Sync[F])
  extends DefaultLDAPGroupGenerator[F](appConfig) {

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
