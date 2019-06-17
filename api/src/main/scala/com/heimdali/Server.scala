package com.heimdali

import java.io.File

import cats.effect.{ExitCode, IO, IOApp, _}
import cats.implicits._
import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.generators._
import com.heimdali.provisioning.DefaultProvisioningService
import com.heimdali.repositories._
import com.heimdali.rest._
import com.heimdali.services._
import com.heimdali.startup.{CacheInitializer, HeimdaliStartup, Provisioning, SessionMaintainer}
import com.typesafe.scalalogging.LazyLogging
import doobie.util.ExecutionContexts
import io.circe.Printer
import io.circe.syntax._
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS
import org.http4s.server.staticcontent._
import org.http4s.server.{Router, Server => H4Server}

import scala.concurrent.duration._

object Server extends IOApp with LazyLogging {

  def createServer[F[_] : ConcurrentEffect : ContextShift : Timer]: Resource[F, H4Server[F]] =
    for {
      context <- AppContext.default[F]()
      _ <- Resource.liftF(
        logger.debug("Config as been read as:\n{}",
          context.appConfig.asJson.pretty(Printer.spaces2)).pure[F])
      provisionEC <- ExecutionContexts.fixedThreadPool(context.appConfig.provisioning.threadPoolSize)
      startupEC <- ExecutionContexts.fixedThreadPool(1)
      _ <- Resource.liftF(logger.info("AppContext has been generated").pure[F])

      configService = new DBConfigService[F](context)

      ldapGroupGenerator = LDAPGroupGenerator.instance(context.appConfig, configService, context.appConfig.templates.ldapGroupGenerator)
      applicationGenerator = ApplicationGenerator.instance(context.appConfig, ldapGroupGenerator, context.appConfig.templates.applicationGenerator)
      topicGenerator = TopicGenerator.instance(context.appConfig, ldapGroupGenerator, context.appConfig.templates.topicGenerator)
      templateService = new JSONTemplateService[F](context, configService)

      provisionService = new DefaultProvisioningService[F](context, provisionEC)
      workspaceService = new WorkspaceServiceImpl[F](provisionService, context)
      accountService = new AccountServiceImpl[F](context, workspaceService, templateService, provisionService)
      memberService = new MemberServiceImpl[F](context)
      kafkaService = new KafkaServiceImpl[F](context, provisionService, topicGenerator)
      applicationService = new ApplicationServiceImpl[F](context, provisionService, applicationGenerator)
      emailService = new EmailServiceImpl[F](context, workspaceService)

      authService = new AuthServiceImpl[F](accountService)
      accountController = new AccountController[F](authService, accountService, context.appConfig)
      templateController = new TemplateController[F](authService, templateService)
      clusterController = new ClusterController[F](context)
      workspaceController = new WorkspaceController[F](authService, workspaceService, memberService, kafkaService, applicationService, emailService, provisionService)
      _ <- Resource.liftF(logger.info("Workspace Controller has been initialized").pure[F])

      memberController = new MemberController[F](authService, memberService)
      riskController = new RiskController[F](authService, workspaceService)
      opsController = new OpsController[F](authService, workspaceService)

      httpApp = Router(
        "/token" -> accountController.basicAuthRoutes,
        "/account" -> accountController.tokenizedRoutes,
        "/auth-type" -> accountController.noAuthRoutes,
        "/templates" -> templateController.route,
        "/clusters" -> clusterController.route,
        "/workspaces" -> workspaceController.route,
        "/members" -> memberController.route,
        "/risk" -> riskController.route,
        "/ops" -> opsController.route,
      ).orNotFound

      provisioningJob = new Provisioning[F](context, provisionService)
      sessionMaintainer = new SessionMaintainer[F](context)
      cacheInitializer = new CacheInitializer[F](context)
      _ <- Resource.liftF(logger.info("Initializing HeimdaliStartup class").pure[F])
      startup = new HeimdaliStartup[F](cacheInitializer, sessionMaintainer, provisioningJob)(startupEC)

      _ <- Resource.liftF(startup.begin())
      _ <- Resource.liftF(logger.info("Class HeimdaliStartup has started").pure[F])

      server <-
        BlazeServerBuilder[F]
          .bindHttp(context.appConfig.rest.port, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .withIdleTimeout(10 minutes)
          .withResponseHeaderTimeout(10 minutes)
          .withSSL(StoreInfo(context.appConfig.rest.sslStore.get, context.appConfig.rest.sslStorePassword.get), context.appConfig.rest.sslKeyManagerPassword.get)
          .resource

      _ <- Resource.liftF(logger.info("Server has started").pure[F])
    } yield server

  override def run(args: List[String]): IO[ExitCode] = {
    logger.info("Server is starting")
    createServer[IO].use(_ => IO.never).as(ExitCode.Success)
  }

}
