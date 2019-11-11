package io.phdata.models

import java.time.Instant

import cats._
import io.circe._

case class HiveAllocation(
    name: String,
    location: String,
    sizeInGB: Int,
    consumedInGB: Option[BigDecimal],
    managingGroup: HiveGrant,
    readWriteGroup: Option[HiveGrant] = None,
    readonlyGroup: Option[HiveGrant] = None,
    id: Option[Long] = None,
    directoryCreated: Option[Instant] = None,
    databaseCreated: Option[Instant] = None,
    protocol: Option[String] = None
) {

  def getProtocol: String =
    if (protocol.isDefined)
      protocol.get
    else {
      if (location.contains("://"))
        location.split("://").head
      else "hdfs"
    }
}

object HiveAllocation {

  def apply(
      name: String,
      location: String,
      sizeInGB: Int,
      managerLDAP: LDAPRegistration,
      readWriteLDAP: Option[LDAPRegistration],
      readonlyLDAP: Option[LDAPRegistration]
  ): HiveAllocation =
    apply(
      name,
      location,
      sizeInGB,
      None,
      HiveGrant(name, location, managerLDAP, Manager),
      readWriteLDAP.map(HiveGrant(name, location, _, Manager)),
      readonlyLDAP.map(HiveGrant(name, location, _, ReadOnly))
    )

  implicit val viewer: Show[HiveAllocation] =
    Show.show(h => s"creating hive database ${h.name}")

  implicit val encoder: Encoder[HiveAllocation] =
    Encoder.forProduct8(
      "id",
      "name",
      "location",
      "size_in_gb",
      "consumed_in_gb",
      "managing_group",
      "readwrite_group",
      "readonly_group"
    )(s => (s.id, s.name, s.location, s.sizeInGB, s.consumedInGB, s.managingGroup, s.readWriteGroup, s.readonlyGroup))

  implicit final val decoder: Decoder[HiveAllocation] =
    Decoder.forProduct6("name", "location", "size_in_gb", "managing_group", "readwrite_group", "readonly_group")(
      (
          name: String,
          location: String,
          size: Int,
          managing: HiveGrant,
          readwrite: Option[HiveGrant],
          readonly: Option[HiveGrant]
      ) => HiveAllocation(name, location, size, None, managing, readwrite, readonly)
    )

}
