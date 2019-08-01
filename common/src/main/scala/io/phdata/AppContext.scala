package io.phdata

import java.io.File

import cats.effect._
import io.phdata.caching.Cached
import io.phdata.config.AppConfig
import com.typesafe.config.{Config, ConfigFactory}
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import io.phdata
import io.phdata.clients._
import io.phdata.repositories.syntax.SqlSyntax
import io.phdata.repositories._
import io.phdata.services._
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.duration._

case class AppContext[F[_]](
    appConfig: AppConfig,
    clusterCache: Cached[F, Seq[Cluster]],
    loginContextProvider: LoginContextProvider,
    sentryClient: SentryClient[F],
    hiveClient: HiveClient[F],
    impalaClient: Option[ImpalaClient[F]],
    provisioningLDAPClient: LDAPClient[F],
    lookupLDAPClient: LDAPClient[F],
    hdfsClient: HDFSClient[F],
    yarnClient: YarnClient[F],
    kafkaClient: KafkaClient[F],
    emailClient: EmailClient[F],
    kerberosClient: KerberosClient[F],
    clusterService: ClusterService[F],
    featureService: FeatureService[F],
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
    configRepository: ConfigRepository
)

object AppContext {

  private val hadoopConfiguration: Configuration = {
    val conf = new Configuration()
    conf.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
    conf.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
    conf.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
    conf.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)
    conf
  }

  private def updateClusterNameservice(currentConfig: AppConfig): AppConfig = {
    if (currentConfig.cluster.nameservice.isEmpty) {
      val newClusterConf = currentConfig.cluster.copy(nameservice = hadoopConfiguration.get("dfs.nameservices"))
      currentConfig.copy(cluster = newClusterConf)
    } else currentConfig
  }

  def default[F[_]: ConcurrentEffect: ContextShift: Timer](
      config: Config = ConfigFactory.defaultApplication().resolve()
  ): Resource[F, AppContext[F]] =
    for {
      loadedConfig <- Resource.liftF(io.circe.config.parser.decodePathF[F, AppConfig](config, "heimdali"))
      config = updateClusterNameservice(loadedConfig)

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
      impalaXA = config.db.impala.map(_.impalaTx)

      httpClient = new CMClient[F](h4Client, config.cluster)

      cacheService = new TimedCacheService()
      clusterCache <- Resource.liftF(cacheService.initial[F, Seq[Cluster]])
      clusterService = new CDHClusterService[F](
        httpClient,
        config.cluster,
        hadoopConfiguration,
        cacheService,
        clusterCache
      )
      featureService = new FeatureServiceImpl[F](config.featureFlags)

      loginContextProvider = new UGILoginContextProvider(config)
      sentryServiceClient = SentryGenericServiceClientFactory.create(hadoopConfiguration)
      sentryClient = new SentryClientImpl[F](hiveXA, sentryServiceClient, loginContextProvider)
      hiveClient = new HiveClientImpl[F](loginContextProvider, hiveXA)
      impalaClient = config.db.impala.map(impalaXA => new ImpalaClientImpl[F](loginContextProvider, impalaXA.impalaTx))
      lookupLDAPClient = new LDAPClientImpl[F](
        config.ldap,
        config => if (config.lookupBinding.server.isEmpty) { config.provisioningBinding } else { config.lookupBinding })
      provisioningLDAPClient = new LDAPClientImpl[F](config.ldap, _.provisioningBinding)
      hdfsClient = new HDFSClientImpl[F](hadoopConfiguration, loginContextProvider)
      yarnClient = new CDHYarnClient[F](httpClient, config.cluster, clusterService)
      kafkaClient = new KafkaClientImpl[F](config)
      emailClient = new EmailClientImpl[F](config, emailEC)
      kerberosClient = new KerberosClientImpl[F](config)

      sqlSyntax = SqlSyntax(config.db.meta.driver)

      complianceRepository = new ComplianceRepositoryImpl
      ldapRepository = new LDAPRepositoryImpl
      hiveDatabaseRepository = new HiveAllocationRepositoryImpl
      yarnRepository = new YarnRepositoryImpl
      workspaceRepository = new WorkspaceRequestRepositoryImpl(sqlSyntax)
      approvalRepository = new ApprovalRepositoryImpl
      memberRepository = new MemberRepositoryImpl(sqlSyntax)
      hiveGrantRepository = new HiveGrantRepositoryImpl
      topicRepository = new KafkaTopicRepositoryImpl
      topicGrantRepository = new TopicGrantRepositoryImpl
      applicationRepository = new ApplicationRepositoryImpl
      configRepository = new ConfigRepositoryImpl
    } yield
      phdata.AppContext[F](
        config,
        clusterCache,
        loginContextProvider,
        sentryClient,
        hiveClient,
        impalaClient,
        provisioningLDAPClient,
        lookupLDAPClient,
        hdfsClient,
        yarnClient,
        kafkaClient,
        emailClient,
        kerberosClient,
        clusterService,
        featureService,
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
