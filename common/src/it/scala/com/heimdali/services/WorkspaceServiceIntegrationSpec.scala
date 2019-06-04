package com.heimdali.services

import java.io.File

import cats.effect._
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.generators._
import com.heimdali.repositories._
import com.heimdali.test.fixtures.{DBTest, _}
import doobie.util.ExecutionContexts
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WorkspaceServiceIntegrationSpec extends FlatSpec with HiveTest with DBTest with Matchers {

  behavior of "Workspace integration spec"

  it should "find non-duplicated records" in new Context {
    implicit val timer = IO.timer(ExecutionContext.global)
    val endall = createService.use { case (service, generator) =>
      for {
        mgr <- generator.generate("edh_sw_mgr", s"edh_sw_mgr,$ldapDn", "role_edh_sw_mgr", initialWorkspaceRequest)
        rw <- generator.generate("edh_sw_rw", s"edh_sw_rw,$ldapDn", "role_edh_sw_rw", initialWorkspaceRequest)
        ro <- generator.generate("edh_sw_ro", s"edh_sw_ro,$ldapDn", "role_edh_sw_ro", initialWorkspaceRequest)
        workspace = initialWorkspaceRequest.copy(data = List(initialHive.copy(managingGroup = initialGrant.copy(ldapRegistration = mgr), readonlyGroup = Some(initialGrant.copy(ldapRegistration = ro)), readWriteGroup = Some(initialGrant.copy(ldapRegistration = rw)))))
        newWorkspace <- service.create(workspace)
        result <- service.find(newWorkspace.id.get).value
      } yield result
    }.unsafeRunSync()
    val managerAttributes = endall.get.data.head.managingGroup.ldapRegistration.attributes
    val readwriteAttributes = endall.get.data.head.readWriteGroup.get.ldapRegistration.attributes
    val readOnlyAttributes = endall.get.data.head.readonlyGroup.get.ldapRegistration.attributes
    
    managerAttributes.distinct.length shouldBe managerAttributes.length
    managerAttributes.map(_._2.replace("edh_sw_", "")) should contain noneOf ("rw", "ro")

    readwriteAttributes.distinct.length shouldBe readwriteAttributes.length
    readwriteAttributes.map(_._2.replace("edh_sw_", "")) should contain noneOf ("mgr", "ro")

    readOnlyAttributes.distinct.length shouldBe readOnlyAttributes.length
    readOnlyAttributes.map(_._2.replace("edh_sw_", "")) should contain noneOf ("rw", "mgr")
  }

  trait Context {
    val hadoopConfiguration = {
      val conf = new Configuration()
      conf.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
      conf.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
      conf.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
      conf.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)
      conf
    }

    def createService(implicit timer: Timer[IO]): Resource[IO, (WorkspaceService[IO], LDAPGroupGenerator[IO])] =
      for {
        httpEC <- ExecutionContexts.fixedThreadPool[IO](10)
        dbConnectionEC <- ExecutionContexts.fixedThreadPool[IO](10)
        dbTransactionEC <- ExecutionContexts.cachedThreadPool[IO]
        emailEC <- ExecutionContexts.fixedThreadPool[IO](10)
        provisionEC <- ExecutionContexts.fixedThreadPool[IO](appConfig.provisioning.threadPoolSize)
        startupEC <- ExecutionContexts.fixedThreadPool[IO](1)

        h4Client = BlazeClientBuilder[IO](httpEC)
          .withRequestTimeout(5 minutes)
          .withResponseHeaderTimeout(5 minutes)
          .resource

        fileReader = new DefaultFileReader[IO]()

        httpClient = new CMClient[IO](h4Client, appConfig.cluster)

        cacheService = new TimedCacheService()
        clusterCache <- Resource.liftF(cacheService.initial[IO, Seq[Cluster]])
        clusterService = new CDHClusterService[IO](httpClient, appConfig.cluster, hadoopConfiguration, cacheService, clusterCache)

        loginContextProvider = new UGILoginContextProvider(appConfig)
        sentryServiceClient = SentryGenericServiceClientFactory.create(hadoopConfiguration)
        sentryClient = new SentryClientImpl[IO](hiveTransactor, sentryServiceClient, loginContextProvider)
        hiveClient = new HiveClientImpl[IO](loginContextProvider, hiveTransactor)
        ldapClient = new LDAPClientImpl[IO](appConfig.ldap, _.provisioningBinding)
        hdfsClient = new HDFSClientImpl[IO](hadoopConfiguration, loginContextProvider)
        yarnClient = new CDHYarnClient[IO](httpClient, appConfig.cluster, clusterService)
        kafkaClient = new KafkaClientImpl[IO](appConfig)
        emailClient = new EmailClientImpl[IO](appConfig, emailEC)

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

        context = AppContext[IO](appConfig, sentryClient, hiveClient, ldapClient, hdfsClient, yarnClient, kafkaClient, transactor, hiveDatabaseRepository, hiveGrantRepository, ldapRepository, memberRepository, yarnRepository, complianceRepository, workspaceRepository, topicRepository, topicGrantRepository, applicationRepository, approvalRepository)

        configService = new DBConfigService[IO](appConfig, configRepository, transactor)

        ldapGroupGenerator = LDAPGroupGenerator.instance(appConfig, configService, appConfig.templates.ldapGroupGenerator)
        applicationGenerator = ApplicationGenerator.instance(appConfig, ldapGroupGenerator, appConfig.templates.applicationGenerator)
        topicGenerator = TopicGenerator.instance(appConfig, ldapGroupGenerator, appConfig.templates.topicGenerator)
        templateService = new JSONTemplateService[IO](appConfig, configService)

        workspaceService = new WorkspaceServiceImpl[IO](null, context)
      } yield (workspaceService, ldapGroupGenerator)
  }

}
