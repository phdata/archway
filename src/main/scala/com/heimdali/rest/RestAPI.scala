package com.heimdali.rest

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import org.http4s.server._
import org.http4s.server.blaze._
import org.http4s.server.middleware.{ CORS, CORSConfig }
import scala.concurrent.duration._

class RestAPI(accountController: AccountController,
              clusterController: ClusterController,
              workspaceController: WorkspaceController,
              templateController: TemplateController,
              memberController: MemberController)
  extends LazyLogging {

  def build(): BlazeBuilder[IO] =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(CORS(router), "/")

  def router = Router[IO](
      "/token" -> accountController.openRoutes,
      "/account" -> accountController.tokenizedRoutes,
      "/templates" -> templateController.route,
      "/clusters" -> clusterController.route,
      "/workspaces" -> workspaceController.route,
      "/members" -> memberController.route
  )

}
