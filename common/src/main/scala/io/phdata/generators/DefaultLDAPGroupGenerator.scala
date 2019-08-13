package io.phdata.generators

import cats.effect.{Clock, Sync}
import cats.implicits._
import io.phdata.models.{DistinguishedName, LDAPRegistration, WorkspaceRequest}
import io.phdata.services.ConfigService

class DefaultLDAPGroupGenerator[F[_]](configService: ConfigService[F])(implicit clock: Clock[F], F: Sync[F])
    extends LDAPGroupGenerator[F] {

  def attributes(cn: String,
                 dn: DistinguishedName,
                 role: String,
                 workspace: WorkspaceRequest): F[List[(String, String)]] =
    configService.getAndSetNextGid.map { gid =>
      List(
        "dn" -> dn.value,
        "objectClass" -> "group",
        "objectClass" -> "top",
        "sAMAccountName" -> cn,
        "cn" -> cn
      )
    }

  def generate(cn: String, dn: DistinguishedName, role: String, workspace: WorkspaceRequest): F[LDAPRegistration] =
    attributes(cn, dn, role, workspace).map(result => LDAPRegistration(dn, cn, role, attributes = result))

}
