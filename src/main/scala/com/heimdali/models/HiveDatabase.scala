package com.heimdali.models

import io.circe._

case class HiveDatabase(name: String,
                        location: String,
                        sizeInGB: Int,
                        managingGroup: LDAPRegistration,
                        readonlyGroup: Option[LDAPRegistration] = None,
                        id: Option[Long] = None)

object HiveDatabase {
  implicit val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit val encoder: Encoder[HiveDatabase] =
    Encoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")(s => (s.name, s.location, s.sizeInGB, s.managingGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveDatabase] =
    Decoder.forProduct5("name", "location", "size_in_gb", "managing_group", "readonly_group")((name: String, location: String, size: Int, managing: LDAPRegistration, readonly: Option[LDAPRegistration]) => HiveDatabase(name, location, size, managing, readonly))

}