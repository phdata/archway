package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS

class RestAPI(accountController: AccountController,
              clusterController: ClusterController,
              workspaceController: WorkspaceController,
              templateController: TemplateController,
              memberController: MemberController)
             (implicit val concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO])
  extends LazyLogging {

  def build(): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(CORS(router.orNotFound))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

  def router: HttpRoutes[IO] = Router(
    "/token" -> accountController.openRoutes,
    "/account" -> accountController.tokenizedRoutes,
    "/templates" -> templateController.route,
    "/clusters" -> clusterController.route,
    "/workspaces" -> workspaceController.route,
    "/members" -> memberController.route
  )

}
