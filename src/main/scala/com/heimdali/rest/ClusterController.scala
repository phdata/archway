package com.heimdali.rest

import akka.http.scaladsl.server.Directives._
import com.heimdali.services.{BasicClusterApp, Cluster, ClusterService, HostClusterApp}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Encoder, Json}

import scala.concurrent.ExecutionContext

class ClusterController(clusterService: ClusterService)
                       (implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  implicit val fooDecoder: Encoder[Cluster] = (a: Cluster) => {
    val services: Map[String, Json] = a.clusterApps.map {
      case (name, BasicClusterApp(display, status, state)) =>
        (name, Json.obj(
          ("state", Json.fromString(state)),
          ("status", Json.fromString(status)),
          ("name", Json.fromString(display))
        ))
      case (name, HostClusterApp(display, status, state, host)) =>
        (name, Json.obj(
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

  val route =
    path("clusters") {
      get {
        onSuccess(clusterService.list) { clusters =>
          complete(clusters)
        }
      }
    }

}
