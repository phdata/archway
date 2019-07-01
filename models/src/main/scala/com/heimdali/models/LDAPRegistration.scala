package com.heimdali.models

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import io.circe._

case class LDAPRegistration(
    distinguishedName: String,
    commonName: String,
    sentryRole: String,
    id: Option[Long] = None,
    groupCreated: Option[Instant] = None,
    roleCreated: Option[Instant] = None,
    roleAssociated: Option[Instant] = None,
    attributes: List[(String, String)] = List.empty
)

object LDAPRegistration {

  implicit val show: Show[LDAPRegistration] =
    Show.show(l => s"creating AD/LDAP group ${l.commonName}")

  implicit val encoder: Encoder[LDAPRegistration] =
    Encoder.forProduct4("common_name", "distinguished_name", "sentry_role", "attributes")(
      s => (s.commonName, s.distinguishedName, s.sentryRole, s.attributes)
    )

  implicit final val decoder: Decoder[LDAPRegistration] =
    Decoder.forProduct4("common_name", "distinguished_name", "sentry_role", "attributes")(
      (cn: String, dn: String, role: String, attributes: List[(String, String)]) =>
        LDAPRegistration(dn, cn, role, attributes = attributes)
    )

}
