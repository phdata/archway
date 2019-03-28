package com.heimdali

import java.io.File
import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.generators.WorkspaceGenerator
import com.heimdali.generators._
import com.heimdali.provisioning.DefaultProvisioningService
import com.heimdali.repositories._
import com.heimdali.rest._
import com.heimdali.services._
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPConnectionPool}
import com.unboundid.util.ssl.{SSLUtil, TrustAllTrustManager}
import doobie.util.ExecutionContexts
import io.circe.config._
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.SSLKeyStoreSupport.StoreInfo
import org.http4s.server.{Server => H4Server, Router, SSLKeyStoreSupport}
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS

import scala.concurrent.ExecutionContext
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

      h4Client = BlazeClientBuilder[F](httpEC)
        .withRequestTimeout(5 minutes)
        .withResponseHeaderTimeout(5 minutes)
        .resource

      metaXA <- config.db.meta.tx(dbConnectionEC, dbTransactionEC)
      hiveXA = config.db.hive.hiveTx

      httpClient = new CMClient[F](h4Client, config.cluster)
      clusterService = new CDHClusterService[F](httpClient, config.cluster, hadoopConfiguration)

      loginContextProvider = new UGILoginContextProvider()
      sentryServiceClient = SentryGenericServiceClientFactory.create(hadoopConfiguration)
      sentryClient = new SentryClientImpl[F](hiveXA, sentryServiceClient, loginContextProvider)
      hiveClient = new HiveClientImpl[F](loginContextProvider, hiveXA)
      ldapClient = new LDAPClientImpl[F](config.ldap) with ActiveDirectoryClient[F]
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

      context = AppContext[F](config, sentryClient, hiveClient, ldapClient, hdfsClient, yarnClient, kafkaClient, metaXA, hiveDatabaseRepository, hiveGrantRepository, ldapRepository, memberRepository, yarnRepository, complianceRepository, workspaceRepository, topicRepository, topicGrantRepository, applicationRepository)

      configService = new DBConfigService[F](config, configRepository, metaXA)

      ldapGroupGenerator = LDAPGroupGenerator.instance(config, configService, _.ldapGroupGenerator)
      applicationGenerator = ApplicationGenerator.instance(config, ldapGroupGenerator, _.applicationGenerator)
      topicGenerator = TopicGenerator.instance(config, ldapGroupGenerator, _.topicGenerator)
      userTemplateGenerator = WorkspaceGenerator.instance[F, WorkspaceGenerator[F, UserTemplate]](config, ldapGroupGenerator, applicationGenerator, topicGenerator, _.userGenerator)
      simpleTemplateGenerator = WorkspaceGenerator.instance[F, WorkspaceGenerator[F, SimpleTemplate]](config, ldapGroupGenerator, applicationGenerator, topicGenerator, _.simpleGenerator)
      structuredTemplateGenerator = WorkspaceGenerator.instance[F, WorkspaceGenerator[F, StructuredTemplate]](config, ldapGroupGenerator, applicationGenerator, topicGenerator, _.structuredGenerator)

      provisionService = new DefaultProvisioningService[F](context)
      workspaceService = new WorkspaceServiceImpl[F](ldapClient, yarnRepository, hiveDatabaseRepository, ldapRepository, workspaceRepository, complianceRepository, approvalRepository, metaXA, memberRepository, topicRepository, applicationRepository, context, provisionService)
      accountService = new AccountServiceImpl[F](ldapClient, config.rest, config.approvers, config.workspaces, workspaceService, userTemplateGenerator, provisionService)
      authService = new AuthServiceImpl[F](accountService)
      memberService = new MemberServiceImpl[F](memberRepository, metaXA, ldapRepository, ldapClient)
      kafkaService = new KafkaServiceImpl[F](context, topicGenerator)
      applicationService = new ApplicationServiceImpl[F](context, applicationGenerator)
      emailService = new EmailServiceImpl[F](emailClient, config, workspaceService, ldapClient)

      accountController = new AccountController[F](authService, accountService)
      templateController = new TemplateController[F](authService, simpleTemplateGenerator, structuredTemplateGenerator)
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
