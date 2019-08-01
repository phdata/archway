package io.phdata.services

import io.circe.{Decoder, HCursor}

object CDHResponses {

  case class ClusterInfo(name: String, displayName: String, fullVersion: String, status: String)

  case class HostRef(hostId: String)

  case class RoleConfigGroupRef(roleConfigGroupName: String)

  case class AppRole(
      name: String,
      `type`: String,
      hostRef: HostRef,
      roleState: String,
      roleConfigGroupRef: RoleConfigGroupRef
  )

  case class Services(items: Seq[ServiceInfo])

  case class ServiceInfo(name: String, `type`: String, serviceState: String, entityStatus: String, displayName: String)

  case class HostInfo(hostId: String, hostname: String)

  case class ListContainer[A](items: List[A])

  case class RoleConfigGroup(name: String, value: Option[String], default: String)

  case class RoleProperty(
      name: String,
      required: Boolean,
      displayName: String,
      description: String,
      relatedName: String,
      sensitive: Boolean,
      validationState: String,
      validationWarningsSuppressed: Option[Boolean],
      value: Option[String],
      default: Option[String]
  )

  implicit val decodeHosInfo: Decoder[HostInfo] =
    Decoder.forProduct2("hostId", "hostname")(HostInfo.apply)

  implicit val decodeServiceInfo: Decoder[ServiceInfo] =
    Decoder.forProduct5("name", "type", "serviceState", "entityStatus", "displayName")(ServiceInfo.apply)

  implicit val decodeHostRef: Decoder[HostRef] =
    Decoder.forProduct1("hostId")(HostRef.apply)

  implicit val decodeRoleConfigGroupRef: Decoder[RoleConfigGroupRef] =
    Decoder.forProduct1("roleConfigGroupName")(RoleConfigGroupRef.apply)

  implicit val decodeImpalaItem: Decoder[AppRole] =
    Decoder.forProduct5("name", "type", "hostRef", "roleState", "roleConfigGroupRef")(AppRole.apply)

  implicit val decodeClusterInfo: Decoder[ClusterInfo] =
    Decoder.forProduct4("name", "displayName", "fullVersion", "entityStatus")(ClusterInfo.apply)

  implicit val decodeRoleConfigGroup: Decoder[RoleConfigGroup] = new Decoder[RoleConfigGroup] {
    final def apply(c: HCursor): Decoder.Result[RoleConfigGroup] =
      for {
        name <- c.downField("name").as[String]
        value <- c.downField("value").as[Option[String]]
        default <- c.downField("default").as[Option[String]]
      } yield {
        RoleConfigGroup(name, value, default.getOrElse(""))
      }
  }

  implicit val decodeYarnRoleProperty: Decoder[RoleProperty] =
    Decoder.forProduct10(
      "name",
      "required",
      "displayName",
      "description",
      "relatedName",
      "sensitive",
      "validationState",
      "validationWarningsSuppressed",
      "value",
      "default"
    )(RoleProperty.apply)
}
