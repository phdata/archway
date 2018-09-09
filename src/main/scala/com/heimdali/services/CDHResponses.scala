package com.heimdali.services

import io.circe.Decoder

object CDHResponses {

  case class ClusterInfo(name: String, displayName: String, fullVersion: String, status: String)

  case class HostRef(hostId: String)

  case class AppRole(name: String, `type`: String, hostRef: HostRef)

  case class Services(items: Seq[ServiceInfo])

  case class ServiceInfo(name: String, `type`: String, serviceState: String, entityStatus: String, displayName: String)

  case class HostInfo(hostId: String, hostname: String)

  case class ListContainer[A](items: List[A])

  implicit val decodeHosInfo: Decoder[HostInfo] =
    Decoder.forProduct2("hostId", "hostname")(HostInfo.apply)

  implicit val decodeServiceInfo: Decoder[ServiceInfo] =
    Decoder.forProduct5("name", "type", "serviceState", "entityStatus", "displayName")(ServiceInfo.apply)

  implicit val decodeHostRef: Decoder[HostRef] =
    Decoder.forProduct1("hostId")(HostRef.apply)

  implicit val decodeImpalaItem: Decoder[AppRole] =
    Decoder.forProduct3("name", "type", "hostRef")(AppRole.apply)

  implicit val decodeClusterInfo: Decoder[ClusterInfo] =
    Decoder.forProduct4("name", "displayName", "fullVersion", "entityStatus")(ClusterInfo.apply)
}
