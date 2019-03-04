package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.config.{AppConfig, RestConfig}
import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.{Router, SSLKeyStoreSupport}
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS

import scala.concurrent.duration._

class RestAPI(accountController: AccountController,
              clusterController: ClusterController,
              workspaceController: WorkspaceController,
              templateController: TemplateController,
              memberController: MemberController,
              riskController: RiskController,
              opsController: OpsController,
              appConfig: AppConfig)
             (implicit val concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO])
  extends LazyLogging {

  def build(): IO[ExitCode] = {
    val baseBuilder =
      BlazeServerBuilder[IO]
        .bindHttp(appConfig.rest.port, "0.0.0.0")
        .withHttpApp(CORS(router.orNotFound))
        .withIdleTimeout(10 minutes)
        .withResponseHeaderTimeout(10 minutes)

    logger.warn("here's config: {}", appConfig)

    val builder = appConfig.rest match {
      case RestConfig(_, _, Some(sslStore), Some(sslStorePassword), Some(sslKeyManagerPassword)) =>
        baseBuilder.withSSL(StoreInfo(sslStore, sslStorePassword), sslKeyManagerPassword)
      case _ =>
        baseBuilder
    }

    builder
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

  def router: HttpRoutes[IO] = Router(
    "/token" -> accountController.openRoutes,
    "/account" -> accountController.tokenizedRoutes,
    "/templates" -> templateController.route,
    "/clusters" -> clusterController.route,
    "/workspaces" -> workspaceController.route,
    "/members" -> memberController.route,
    "/risk" -> riskController.route,
    "/ops" -> opsController.route,
  )

}
