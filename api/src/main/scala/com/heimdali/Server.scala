package com.heimdali

import java.io.File

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.generators._
import com.heimdali.provisioning.DefaultProvisioningService
import com.heimdali.repositories._
import com.heimdali.rest._
import com.heimdali.services.{TemplateService, _}
import com.heimdali.startup.{HeimdaliStartup, Provisioning, SessionMaintainer}
import doobie.util.ExecutionContexts
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS

import scala.concurrent.duration._

object Server extends IOApp {

  val hadoopConfiguration = {
    val conf = new Configuration()
    conf.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
    conf.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
    conf.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
    conf.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)
    conf
  }

  def createServer[F[_] : ConcurrentEffect : ContextShift : Timer]: Resource[F, H4Server[F]] =
    for {
      config <- Resource.liftF(io.circe.config.parser.decodePathF[F, AppConfig]("heimdali"))

      httpEC <- ExecutionContexts.fixedThreadPool(10)
      dbConnectionEC <- ExecutionContexts.fixedThreadPool(10)
      dbTransactionEC <- ExecutionContexts.cachedThreadPool
      emailEC <- ExecutionContexts.fixedThreadPool(10)
      provisionEC <-   ExecutionContexts.fixedThreadPool(config.provisioning.threadPoolSize)
      startupEC <- ExecutionContexts.fixedThreadPool(1)

      h4Client = BlazeClientBuilder[F](httpEC)
        .withRequestTimeout(5 minutes)
        .withResponseHeaderTimeout(5 minutes)
        .resource

      metaXA <- config.db.meta.tx(dbConnectionEC, dbTransactionEC)
      hiveXA = config.db.hive.hiveTx

      fileReader = new DefaultFileReader[F]()

      httpClient = new CMClient[F](h4Client, config.cluster)
      clusterService = new CDHClusterService[F](httpClient, config.cluster, hadoopConfiguration)

      loginContextProvider = new UGILoginContextProvider()
      sentryServiceClient = SentryGenericServiceClientFactory.create(hadoopConfiguration)
      sentryClient = new SentryClientImpl[F](hiveXA, sentryServiceClient, loginContextProvider)
      hiveClient = new HiveClientImpl[F](loginContextProvider, hiveXA)
      lookupLDAPClient = new LDAPClientImpl[F](config.ldap, _.lookupBinding)
      provisioningLDAPClient = new LDAPClientImpl[F](config.ldap, _.provisioningBinding)
      hdfsClient = new HDFSClientImpl[F](hadoopConfiguration, loginContextProvider)
      yarnClient = new CDHYarnClient[F](httpClient, config.cluster, clusterService)
      kafkaClient = new KafkaClientImpl[F](config)
      emailClient = new EmailClientImpl[F](config, emailEC)

      complianceRepository = new ComplianceRepositoryImpl
      ldapRepository = new LDAPRepositoryImpl
      hiveDatabaseRepository = new HiveAllocationRepositoryImpl
      yarnRepository = new YarnRepositoryImpl
      workspaceRepository = new WorkspaceRequestRepositoryImpl
      approvalRepository = new ApprovalRepositoryImpl
      memberRepository = new MemberRepositoryImpl
      hiveGrantRepository = new HiveGrantRepositoryImpl
      topicRepository = new KafkaTopicRepositoryImpl
      topicGrantRepository = new TopicGrantRepositoryImpl
      applicationRepository = new ApplicationRepositoryImpl
      configRepository = new ConfigRepositoryImpl

      context = AppContext[F](config, sentryClient, hiveClient, provisioningLDAPClient, hdfsClient, yarnClient, kafkaClient, metaXA, hiveDatabaseRepository, hiveGrantRepository, ldapRepository, memberRepository, yarnRepository, complianceRepository, workspaceRepository, topicRepository, topicGrantRepository, applicationRepository)
      _ <- Resource.liftF(logger.debug("AppContext has been generated").pure[F])

      configService = new DBConfigService[F](config, configRepository, metaXA)

      ldapGroupGenerator = LDAPGroupGenerator.instance(config, configService, config.templates.ldapGroupGenerator)
      applicationGenerator = ApplicationGenerator.instance(config, ldapGroupGenerator, config.templates.applicationGenerator)
      topicGenerator = TopicGenerator.instance(config, ldapGroupGenerator, config.templates.topicGenerator)
      templateService = new JSONTemplateService[F](config, fileReader, configService)

      provisionService = new DefaultProvisioningService[F](context, provisionEC)
      workspaceService = new WorkspaceServiceImpl[F](provisioningLDAPClient, yarnRepository, hiveDatabaseRepository, ldapRepository, workspaceRepository, complianceRepository, approvalRepository, metaXA, memberRepository, topicRepository, applicationRepository, context, provisionService)
      accountService = new AccountServiceImpl[F](lookupLDAPClient, config.rest, config.approvers, config.workspaces, workspaceService, templateService, provisionService)
      authService = new AuthServiceImpl[F](accountService)
      memberService = new MemberServiceImpl[F](memberRepository, metaXA, ldapRepository, lookupLDAPClient, provisioningLDAPClient)
      kafkaService = new KafkaServiceImpl[F](context, provisionService, topicGenerator)
      applicationService = new ApplicationServiceImpl[F](context, provisionService, applicationGenerator)
      emailService = new EmailServiceImpl[F](emailClient, config, workspaceService, lookupLDAPClient)

      accountController = new AccountController[F](authService, accountService)
      templateController = new TemplateController[F](authService, templateService)
      clusterController = new ClusterController[F](clusterService)
      workspaceController = new WorkspaceController[F](authService, workspaceService, memberService, kafkaService, applicationService, emailService, provisionService)
      memberController = new MemberController[F](authService, memberService)
      riskController = new RiskController[F](authService, workspaceService)
      opsController = new OpsController[F](authService, workspaceService)

      httpApp = Router(
      "/token" -> accountController.openRoutes,
      "/account" -> accountController.tokenizedRoutes,
      "/templates" -> templateController.route,
      "/clusters" -> clusterController.route,
      "/workspaces" -> workspaceController.route,
      "/members" -> memberController.route,
      "/risk" -> riskController.route,
        "/ops" -> opsController.route,
      ).orNotFound

      provisioningJob =  new Provisioning[F](config.provisioning, provisionService)
      sessionMaintainer = new SessionMaintainer[F](config.cluster, loginContextProvider)
      startup = new HeimdaliStartup[F](provisioningJob, sessionMaintainer)(startupEC)

      _ <- Resource.liftF(startup.begin())

      server <-
        BlazeServerBuilder[F]
          .bindHttp(config.rest.port, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .withIdleTimeout(10 minutes)
          .withResponseHeaderTimeout(10 minutes)
          .withSSL(StoreInfo(config.rest.sslStore.get, config.rest.sslStorePassword.get), config.rest.sslKeyManagerPassword.get)
          .resource
    } yield server

  override def run(args: List[String]): IO[ExitCode] =
    createServer[IO].use(_ => IO.never).as(ExitCode.Success)
}
