/* Copyright 2018 phData Inc. */

package com.heimdali.provisioning

import java.io.File

import cats.effect.{ExitCode, IO, IOApp, _}
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.clients.{ActiveDirectoryClient, _}
import com.heimdali.config.AppConfig
import cats.effect.Timer
import com.heimdali.repositories._
import com.heimdali.services._
import doobie.util.ExecutionContexts
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.duration._

object ProvisioningApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    createProvisioningService[IO]().use { provisioningService =>
      fs2.Stream.awakeEvery[IO](15 minutes)
        .evalMap(_ => provisioningService.provisionAll())
        .compile
        .drain
    }.as(ExitCode.Success)
  }

  def createProvisioningService[F[_]: ConcurrentEffect: ContextShift: Timer]()
    : Resource[F, ProvisioningService[F]] = {
    val hadoopConfiguration = {
      val conf = new Configuration()
      conf.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
      conf.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
      conf.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
      conf.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)
      conf
    }

    for {
      config <- Resource.liftF(io.circe.config.parser.decodePathF[F, AppConfig]("heimdali"))

      httpEC <- ExecutionContexts.fixedThreadPool(10)
      dbConnectionEC <- ExecutionContexts.fixedThreadPool(10)
      dbTransactionEC <- ExecutionContexts.cachedThreadPool
      provisionEC <-   ExecutionContexts.fixedThreadPool(10)

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

      complianceRepository = new ComplianceRepositoryImpl
      ldapRepository = new LDAPRepositoryImpl
      hiveDatabaseRepository = new HiveAllocationRepositoryImpl
      yarnRepository = new YarnRepositoryImpl
      workspaceRepository = new WorkspaceRequestRepositoryImpl
      memberRepository = new MemberRepositoryImpl
      hiveGrantRepository = new HiveGrantRepositoryImpl
      topicRepository = new KafkaTopicRepositoryImpl
      topicGrantRepository = new TopicGrantRepositoryImpl
      applicationRepository = new ApplicationRepositoryImpl

      context = AppContext[F](
        config,
        sentryClient,
        hiveClient,
        ldapClient,
        hdfsClient,
        yarnClient,
        kafkaClient,
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
        applicationRepository
      )

    } yield new DefaultProvisioningService[F](context, provisionEC)
  }
}
