package com.heimdali.models

import java.time.Instant

import cats._
import io.circe._
import io.circe.java8.time._

case class HiveGrant(
    databaseName: String,
    location: String,
    ldapRegistration: LDAPRegistration,
    databaseRole: DatabaseRole,
    id: Option[Long] = None,
    locationAccess: Option[Instant] = None,
    databaseAccess: Option[Instant] = None
)

object HiveGrant {

  implicit val show: Show[HiveGrant] =
    Show.show(g => s"granting ${g.ldapRegistration.commonName} access to ${g.databaseName}")

  implicit val encoder: Encoder[HiveGrant] =
    Encoder.forProduct3("location_access", "database_access", "group")(
      g => (g.locationAccess, g.databaseAccess, g.ldapRegistration)
    )

  implicit val decoder: Decoder[HiveGrant] =
    Decoder.forProduct1("group")((g: LDAPRegistration) => HiveGrant("", "", g, ReadOnly))

}
