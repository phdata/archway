package com.heimdali.models

import java.time.Instant

import cats._
import io.circe._

case class HiveAllocation(name: String,
                          location: String,
                          sizeInGB: Int,
                          consumedInGB: Option[BigDecimal],
                          managingGroup: HiveGrant,
                          readonlyGroup: Option[HiveGrant] = None,
                          id: Option[Long] = None,
                          directoryCreated: Option[Instant] = None,
                          databaseCreated: Option[Instant] = None)

object HiveAllocation {

  def apply(name: String,
            location: String,
            sizeInGB: Int,
            managerLDAP: LDAPRegistration,
            readonlyLDAP: Option[LDAPRegistration]): HiveAllocation =
    apply(name, location, sizeInGB, None, HiveGrant(name, location, managerLDAP, Manager), readonlyLDAP.map(ldap => HiveGrant(name, location, ldap, ReadOnly)))

  implicit val viewer: Show[HiveAllocation] =
    Show.show(h => s"creating hive database ${h.name}")

  implicit val encoder: Encoder[HiveAllocation] =
    Encoder.forProduct7("id", "name", "location", "size_in_gb", "consumed_in_gb", "managing_group", "readonly_group")(s => (s.id, s.name, s.location, s.sizeInGB, s.consumedInGB, s.managingGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveAllocation] =
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: HiveGrant, readonly: Option[HiveGrant]) =>
      HiveAllocation(name, location, size, None, managing, readonly))

}
