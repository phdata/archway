package com.heimdali.services

import com.heimdali.services.CDHResponses.{AppRole, HostInfo, ListContainer, ServiceInfo}
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._

trait ClusterService[F[_]] {
  def list: F[Seq[Cluster]]
}

case class AppLocation(host: String, port: Int)

case class ClusterApp(id: String, name: String, state: String, status: String, capabilities: Map[String, List[AppLocation]])

object ClusterApp {
  def apply(name: String, serviceInfo: ServiceInfo, hosts: ListContainer[HostInfo], appRoles: Map[String, (Int, List[AppRole])]): ClusterApp = {
    ClusterApp(
      serviceInfo.name,
      name,
      serviceInfo.serviceState,
      serviceInfo.entityStatus,
      appRoles.map { r =>
        r._1 -> r._2._2.map { ri =>
          AppLocation(hosts.items.find(h => ri.hostRef.hostId == h.hostId).get.hostname, r._2._1)
        }
      }
    )
  }

  implicit val decoder: Encoder[ClusterApp] =
    Encoder.instance { a =>
      a.capabilities.foldLeft(Json.obj(
        "state" -> a.state.asJson,
        "status" -> a.status.asJson,
      )) { (existing, next) =>
        existing.deepMerge(Json.obj(next._1 -> next._2.asJson))
      }
    }
}

case class Cluster(id: String, name: String, cmURL: String, services: List[ClusterApp], distribution: CDH, status: String)

object Cluster {
  implicit val decoder: Encoder[Cluster] =
    Encoder.instance { a =>
      Json.obj(
        "id" -> a.id.asJson,
        "name" -> a.name.asJson,
        "cm_url" -> a.cmURL.asJson,
        "services" -> a.services.foldLeft(Json.obj())((existing, next) => existing.deepMerge(Json.obj(next.name -> next.asJson))),
        "distribution" -> Json.obj(
          "name" -> a.distribution.name.asJson,
          "version" -> a.distribution.version.asJson
        ),
        "status" -> a.status.asJson
      )
    }
}

sealed trait Distribution {
  def version: String

  def name: String
}

case class CDH(version: String) extends Distribution {
  val name = "CDH"
}