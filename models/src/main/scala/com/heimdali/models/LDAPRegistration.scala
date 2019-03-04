package com.heimdali.models

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import io.circe._

case class LDAPRegistration(distinguishedName: String,
                            commonName: String,
                            sentryRole: String,
                            id: Option[Long] = None,
                            groupCreated: Option[Instant] = None,
                            roleCreated: Option[Instant] = None,
                            roleAssociated: Option[Instant] = None,
                            attributes: Map[String, String] = Map.empty)

object LDAPRegistration {

  implicit val show: Show[LDAPRegistration] =
    Show.show(l => s"creating AD/LDAP group ${l.commonName}")

  implicit val encoder: Encoder[LDAPRegistration] =
    Encoder.forProduct3("common_name", "distinguished_name", "sentry_role")(s => (s.commonName, s.distinguishedName, s.sentryRole))

  implicit final val decoder: Decoder[LDAPRegistration] =
    Decoder.forProduct3("common_name", "distinguished_name", "sentry_role")((cn: String, dn: String, role: String) => LDAPRegistration(dn, cn, role))

}