package io.phdata.generators

import cats.effect.{Clock, Sync}
import cats.implicits._
import io.phdata.models.{DistinguishedName, LDAPRegistration}
import io.phdata.services.ConfigService

class DefaultLDAPGroupGenerator[F[_]](configService: ConfigService[F])(implicit clock: Clock[F], F: Sync[F])
    extends LDAPGroupGenerator[F] {

  def attributes(
      cn: String,
      dn: DistinguishedName,
      role: String
  ): F[List[(String, String)]] =
    configService.getAndSetNextGid.map { _ =>
      List(
        "dn" -> dn.value,
        "objectClass" -> "group",
        "objectClass" -> "top",
        "sAMAccountName" -> cn,
        "cn" -> cn
      )
    }

  def generate(cn: String, dn: DistinguishedName, role: String): F[LDAPRegistration] =
    attributes(cn, dn, role).map(result => LDAPRegistration(dn, cn, role, attributes = result))

}
