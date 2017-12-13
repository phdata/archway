package com.heimdali

import akka.http.scaladsl.server.Directives._
import com.heimdali.services.{Cluster, ClusterService}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Encoder, Json}

import scala.concurrent.ExecutionContext

class ClusterController(clusterService: ClusterService)
                       (implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  implicit val fooDecoder: Encoder[Cluster] = (a: Cluster) => Json.obj(
    ("id", Json.fromString(a.id)),
    ("name", Json.fromString(a.name)),
    ("distribution", Json.obj(
      ("name", Json.fromString(a.distribution.name)),
      ("version", Json.fromString(a.distribution.version))
    )))

  val route =
    path("clusters") {
      get {
        onSuccess(clusterService.list) { clusters =>
          complete(clusters)
        }
      }
    }

}
