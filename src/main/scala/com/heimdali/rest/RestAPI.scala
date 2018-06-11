package com.heimdali.rest

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import org.http4s.server.blaze._

class RestAPI(accountController: AccountController,
              clusterController: ClusterController,
              workspaceController: WorkspaceController,
              templateController: TemplateController)
  extends LazyLogging {

  def build(): BlazeBuilder[IO] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(accountController.openRoutes, "/token")
      .mountService(accountController.tokenizedRoutes, "/account")
      .mountService(templateController.route, "/templates")
      .mountService(clusterController.route, "/clusters")
      .mountService(workspaceController.route, "/workspaces")

}