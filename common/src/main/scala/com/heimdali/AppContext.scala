package com.heimdali

import java.io.File

import cats.effect.{ExitCode, IO, IOApp, _}
import cats.implicits._
import com.heimdali.caching.Cached
import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.generators._
import com.heimdali.repositories._
import com.heimdali.services._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
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

case class AppContext[F[_]](appConfig: AppConfig,
                            clusterCache: Cached[F, Seq[Cluster]],
                            loginContextProvider: LoginContextProvider,
                            sentryClient: SentryClient[F],
                            hiveClient: HiveClient[F],
                            provisioningLDAPClient: LDAPClient[F],
                            lookupLDAPClient: LDAPClient[F],
                            hdfsClient: HDFSClient[F],
                            yarnClient: YarnClient[F],
                            kafkaClient: KafkaClient[F],
                            emailClient: EmailClient[F],
                            clusterService: ClusterService[F],
                            transactor: Transactor[F],
                            databaseRepository: HiveAllocationRepository,
                            databaseGrantRepository: HiveGrantRepository,
                            ldapRepository: LDAPRepository,
                            memberRepository: MemberRepository,
                            yarnRepository: YarnRepository,
                            complianceRepository: ComplianceRepository,
                            workspaceRequestRepository: WorkspaceRequestRepository,
                            kafkaRepository: KafkaTopicRepository,
                            topicGrantRepository: TopicGrantRepository,
                            applicationRepository: ApplicationRepository,
                            approvalRepository: ApprovalRepository,
                            configRepository: ConfigRepository)

object AppContext {

  private val hadoopConfiguration: Configuration = {
    val conf = new Configuration()
    conf.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
    conf.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
    conf.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
    conf.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)
    conf
  }

  def default[F[_] : ConcurrentEffect : ContextShift : Timer](config: Config = ConfigFactory.defaultApplication().resolve()): Resource[F, AppContext[F]] =
    for {
      config <- Resource.liftF(io.circe.config.parser.decodePathF[F, AppConfig](config, "heimdali"))

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

      cacheService = new TimedCacheService()
      clusterCache <- Resource.liftF(cacheService.initial[F, Seq[Cluster]])
      clusterService = new CDHClusterService[F](httpClient, config.cluster, hadoopConfiguration, cacheService, clusterCache)

      loginContextProvider = new UGILoginContextProvider(config)
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
    } yield AppContext[F](
      config,
      clusterCache,
      loginContextProvider,
      sentryClient,
      hiveClient,
      provisioningLDAPClient,
      lookupLDAPClient,
      hdfsClient,
      yarnClient,
      kafkaClient,
      emailClient,
      clusterService,
      metaXA,
      hiveDatabaseRepository,
      hiveGrantRepository,
      ldapRepository,
      memberRepository,
      yarnRepository,
      complianceRepository,
      workspaceRepository,
      topicRepository,
      topicGrantRepository,
      applicationRepository,
      approvalRepository,
      configRepository
    )
}