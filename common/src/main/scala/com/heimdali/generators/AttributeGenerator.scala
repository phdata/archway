package com.heimdali.generators

import cats._
import cats.implicits._
import com.heimdali.config.LDAPConfig
import com.heimdali.models.{LDAPRegistration, WorkspaceRequest}

trait AttributeGenerator[F[_]] {

  def ldapConfig: LDAPConfig

  def generate(cn: String, dn: String, role: String)(implicit evidence: Monad[F]): F[LDAPRegistration] =
    LDAPRegistration(dn, cn, role, attributes =
      List(
        "dn" -> dn,
        "objectClass" -> "group",
        "objectClass" -> "top",
        "sAMAccountName" -> cn,
        "cn" -> cn,
        "msSFU30Name" -> cn,
        "msSFU30NisDomain" -> ldapConfig.domain
      )).pure[F]

}
