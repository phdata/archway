package com.heimdali.generators

import java.time.Clock

import cats._
import cats.effect.Sync
import cats.implicits._
import com.heimdali.config.AppConfig
import com.heimdali.models.{LDAPRegistration, WorkspaceRequest}

class DefaultLDAPGroupGenerator[F[_]](appConfig: AppConfig)
                                     (implicit clock: Clock, F: Sync[F])
  extends LDAPGroupGenerator[F] {

  def attributes(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[List[(String, String)]] =
    List(
      "dn" -> dn,
      "objectClass" -> "group",
      "objectClass" -> "top",
      "sAMAccountName" -> cn,
      "cn" -> cn,
      "msSFU30Name" -> cn,
      "msSFU30NisDomain" -> appConfig.ldap.domain
    ).pure[F]

  def generate(cn: String, dn: String, role: String, workspace: WorkspaceRequest): F[LDAPRegistration] =
    attributes(cn, dn, role, workspace)
      .map(result => LDAPRegistration(dn, cn, role, attributes = result))

}
