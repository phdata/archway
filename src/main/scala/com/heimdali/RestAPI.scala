package com.heimdali

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class RestAPI (accountController: AccountController,
                   clusterController: ClusterController,
                   workspaceController: WorkspaceController) {

  val route: Route =
    accountController.route ~ clusterController.route ~ workspaceController.route

}