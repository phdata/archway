package com.heimdali.services

import io.circe.{Encoder, Json}

trait ClusterService[F[_]] {
  def list: F[Seq[Cluster]]
}

sealed trait ClusterApp {
  def id: String
  def name: String
  def status: String
  def state: String
}

case class BasicClusterApp(id: String, name: String, status: String, state: String) extends ClusterApp

case class HostClusterApp(id: String, name: String, status: String, state: String, host: String) extends ClusterApp

case class Cluster(id: String, name: String, clusterApps: Map[String, _ <: ClusterApp], distribution: CDH, status: String)

object Cluster {
  implicit val fooDecoder: Encoder[Cluster] = (a: Cluster) => {
    val services: Map[String, Json] = a.clusterApps.map {
      case (name, BasicClusterApp(id, display, status, state)) =>
        (name, Json.obj(
          ("id", Json.fromString(id)),
          ("state", Json.fromString(state)),
          ("status", Json.fromString(status)),
          ("name", Json.fromString(display))
        ))
      case (name, HostClusterApp(id, display, status, state, host)) =>
        (name, Json.obj(
          ("id", Json.fromString(id)),
          ("state", Json.fromString(state)),
          ("status", Json.fromString(status)),
          ("name", Json.fromString(display)),
          ("host", Json.fromString(host))
        ))
    }
    Json.obj(
      ("id", Json.fromString(a.id)),
      ("name", Json.fromString(a.name)),
      ("services", Json.obj(services.to:_*)),
      ("distribution", Json.obj(
        ("name", Json.fromString(a.distribution.name)),
        ("version", Json.fromString(a.distribution.version))
      )),
      ("status", Json.fromString(a.status)))
  }
}

sealed trait Distribution {
  def version: String
  def name: String
}

case class CDH(version: String) extends Distribution {
  val name = "CDH"
}