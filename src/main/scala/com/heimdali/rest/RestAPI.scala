package com.heimdali.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class RestAPI(http: HttpExt,
              configuration: Config,
              accountController: AccountController,
              clusterController: ClusterController,
              workspaceController: WorkspaceController)
             (implicit actorSystem: ActorSystem,
              materializer: Materializer,
              executionContext: ExecutionContext) extends LazyLogging {

  val route: Route =
    accountController.route ~ clusterController.route ~ workspaceController.route

  val port: Int = configuration.getInt("rest.port")

  def start(): Future[Unit] = {
    http.bindAndHandle(route, "0.0.0.0", port = port) map {
      binding =>
        logger.info(s"REST interface bound to ${binding.localAddress}")
    } recover {
      case ex =>
        logger.error(s"REST interface could not bind", ex.getMessage)
    }
  }

}