package com.heimdali

import akka.http.scaladsl.server.Directives._
import com.heimdali.services.ClusterService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

import scala.concurrent.ExecutionContext

class ClusterController(clusterService: ClusterService)
                       (implicit executionContext: ExecutionContext)
  extends FailFastCirceSupport {

  import io.circe.java8.time._
  import io.circe.generic.auto._

  val route =
    path("clusters") {
      get {
        onSuccess(clusterService.list) { clusters =>
          complete(clusters)
        }
      }
    }

}
