package io.phdata

import cats.effect.{ExitCode, IO, IOApp, _}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie.util.ExecutionContexts
import io.circe.Printer
import io.circe.syntax._
import io.phdata.generators.{ApplicationGenerator, LDAPGroupGenerator, TopicGenerator}
import io.phdata.provisioning.DefaultProvisioningService
import io.phdata.rest.authentication.{LdapAuthService, SpnegoAuthService, TokenAuthServiceImpl}
import io.phdata.rest._
import io.phdata.services._
import io.phdata.startup.{ADGroupsSynchronizer, ArchwayStartup, CacheInitializer, Provisioning, SessionMaintainer}
import org.http4s.implicits._
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.blaze._
import org.http4s.server.{Router, Server => H4Server}

import scala.concurrent.duration._

object Server extends IOApp with LazyLogging {

  def createServer[F[_]: ConcurrentEffect: ContextShift: Timer]: Resource[F, H4Server[F]] =
    for {
      context <- AppContext.default[F]()
      _ <- Resource.liftF(
        logger.debug("Config as been read as:\n{}", context.appConfig.asJson.pretty(Printer.spaces2)).pure[F]
      )
      provisionEC <- ExecutionContexts.fixedThreadPool(context.appConfig.provisioning.threadPoolSize)
      startupEC <- ExecutionContexts.fixedThreadPool(1)
      staticContentEC <- ExecutionContexts.fixedThreadPool(1)
      emailEC <- ExecutionContexts.fixedThreadPool(1)
      _ <- Resource.liftF(logger.info("AppContext has been generated").pure[F])

      configService = new DBConfigService[F](context)
      _ <- Resource.liftF(configService.verifyDbConnection)

      ldapGroupGenerator = LDAPGroupGenerator
        .instance(context.appConfig, configService, context.appConfig.templates.ldapGroupGenerator)
      applicationGenerator = ApplicationGenerator
        .instance(context.appConfig, ldapGroupGenerator, context.appConfig.templates.applicationGenerator)
      topicGenerator = TopicGenerator
        .instance(context.appConfig, ldapGroupGenerator, context.appConfig.templates.topicGenerator)
      templateService = new JSONTemplateService[F](context, configService)
      _ <- Resource.liftF(templateService.verifyDefaultTemplates)
      _ <- Resource.liftF(templateService.verifyCustomTemplates)

      provisionService = new DefaultProvisioningService[F](context, provisionEC)
      memberService = new MemberServiceImpl[F](context)
      workspaceService = new WorkspaceServiceImpl[F](provisionService, memberService, context)
      accountService = new AccountServiceImpl[F](context, workspaceService, templateService, provisionService)
      kafkaService = new KafkaServiceImpl[F](context, provisionService, topicGenerator)
      applicationService = new ApplicationServiceImpl[F](context, provisionService, applicationGenerator)
      emailService = new EmailServiceImpl[F](context, workspaceService)
      complianceService = new ComplianceGroupServiceImpl[F](context)
      _ <- Resource.liftF(complianceService.loadDefaultComplianceQuestions)
      customLinkService = new CustomLinkGroupServiceImpl[F](context)

      authService = context.appConfig.rest.authType match {
        case "spnego" =>
          logger.info("Choosing spnego auth service")
          new SpnegoAuthService[F](accountService)
        case "ldap" =>
          logger.info("Choosing ldap auth service")
          new LdapAuthService[F](accountService)
        case authType =>
          logger.warn(s"Auth type not recognized '$authType', falling back to ldap auth")
          new LdapAuthService[F](accountService)
      }

      tokenAuthService = new TokenAuthServiceImpl[F](accountService)
      accountController = new AccountController[F](authService, tokenAuthService, accountService, context)
      templateController = new TemplateController[F](tokenAuthService, templateService)
      clusterController = new ClusterController[F](tokenAuthService, context.clusterService)
      workspaceController = new WorkspaceController[F](
        tokenAuthService,
        workspaceService,
        memberService,
        kafkaService,
        applicationService,
        emailService,
        provisionService,
        complianceService,
        emailEC
      )
      _ <- Resource.liftF(logger.debug("Workspace Controller has been initialized").pure[F])

      memberController = new MemberController[F](tokenAuthService, memberService)
      riskController = new RiskController[F](tokenAuthService, workspaceService)
      opsController = new OpsController[F](tokenAuthService, workspaceService, customLinkService)
      staticContentController = new StaticContentController[F](context, staticContentEC)

      defaultResponseMiddleware = new DefaultResponseMiddleware[F]

      httpApp = Router(
        "/token" -> defaultResponseMiddleware.apply(accountController.clientAuthRoutes),
        "/account" -> defaultResponseMiddleware.apply(accountController.tokenizedRoutes),
        "/auth-type" -> defaultResponseMiddleware.apply(accountController.noAuthRoutes),
        "/templates" -> defaultResponseMiddleware.apply(templateController.route),
        "/clusters" -> defaultResponseMiddleware.apply(clusterController.route),
        "/workspaces" -> defaultResponseMiddleware.apply(workspaceController.route),
        "/members" -> defaultResponseMiddleware.apply(memberController.route),
        "/risk" -> defaultResponseMiddleware.apply(riskController.route),
        "/ops" -> defaultResponseMiddleware.apply(opsController.route),
        "/" -> defaultResponseMiddleware.apply(staticContentController.route)
      ).orNotFound

      provisioningJob = new Provisioning[F](context, provisionService)
      sessionMaintainer = new SessionMaintainer[F](context)
      cacheInitializer = new CacheInitializer[F](context)
      adGroupsSynchronizer = new ADGroupsSynchronizer[F](context)
      _ <- Resource.liftF(logger.info("Initializing ArchwayStartup class").pure[F])
      startup = new ArchwayStartup[F](cacheInitializer, sessionMaintainer, provisioningJob, adGroupsSynchronizer)(
        startupEC
      )

      _ <- Resource.liftF(startup.begin())
      _ <- Resource.liftF(logger.info("Class ArchwayStartup has started").pure[F])

      server <- BlazeServerBuilder[F]
        .bindHttp(context.appConfig.rest.port, "0.0.0.0")
        .withHttpApp(httpApp)
        .withIdleTimeout(10 minutes)
        .withResponseHeaderTimeout(10 minutes)
        .withSSL(
          StoreInfo(context.appConfig.rest.sslStore.get, context.appConfig.rest.sslStorePassword.get.value),
          context.appConfig.rest.sslKeyManagerPassword.get.value
        )
        .resource

      _ <- Resource.liftF(logger.info("Server has started").pure[F])
    } yield server

  override def run(args: List[String]): IO[ExitCode] = {
    logger.info("Server is starting")
    createServer[IO]
      .use(_ => IO.never)
      .redeemWith(
        ex => {
          logger.error(s"Starting server failed ${ex.getLocalizedMessage}", ex)
          IO.pure(ExitCode.Error)
        },
        _ => IO.pure(ExitCode.Success)
      )
  }

}
