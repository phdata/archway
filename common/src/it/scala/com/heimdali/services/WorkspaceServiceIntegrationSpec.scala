package com.heimdali.services

import java.io.File

import cats.effect._
import cats.effect.implicits._
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.test.fixtures._
import com.heimdali.generators.{ApplicationGenerator, LDAPGroupGenerator, TopicGenerator, WorkspaceGenerator}
import com.heimdali.models.{SimpleTemplate, StructuredTemplate, UserTemplate}
import com.heimdali.repositories._

import scala.concurrent.duration._
import com.heimdali.test.fixtures.DBTest
import doobie.util.ExecutionContexts
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class WorkspaceServiceIntegrationSpec extends FlatSpec with HiveTest with DBTest with Matchers {

  behavior of "Workspace integration spec"

  it should "find non-duplicated records" in new Context {
    implicit val timer = IO.timer(ExecutionContext.global)
    val endall = createService.use { case (service, generator) =>
      for {
        ldap1 <- generator.generate(initialLDAP.commonName, initialLDAP.distinguishedName, initialLDAP.sentryRole, initialWorkspaceRequest)
        ldap2 <- generator.generate(initialLDAP.commonName, initialLDAP.distinguishedName, initialLDAP.sentryRole, initialWorkspaceRequest)
        ldap3 <- generator.generate(initialLDAP.commonName, initialLDAP.distinguishedName, initialLDAP.sentryRole, initialWorkspaceRequest)
        workspace = initialWorkspaceRequest.copy(data = List(initialHive.copy(managingGroup = initialGrant.copy(ldapRegistration = ldap1), readonlyGroup = Some(initialGrant.copy(ldapRegistration = ldap2)), readWriteGroup = Some(initialGrant.copy(ldapRegistration = ldap3)))))
        newWorkspace <- service.create(workspace)
        result <- service.find(newWorkspace.id.get).value
      } yield result
    }.unsafeRunSync()
    val managerAttributes = endall.get.data.head.managingGroup.ldapRegistration.attributes
    val readwriteAttributes = endall.get.data.head.readWriteGroup.get.ldapRegistration.attributes
    val readOnlyAttributes = endall.get.data.head.readonlyGroup.get.ldapRegistration.attributes
    
    managerAttributes.distinct.length shouldBe managerAttributes.length
    readwriteAttributes.distinct.length shouldBe readwriteAttributes.length
    readOnlyAttributes.distinct.length shouldBe readOnlyAttributes.length
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

    def createService: Resource[IO, (WorkspaceService[IO], LDAPGroupGenerator[IO])] =
      for {
        config <- Resource.liftF(io.circe.config.parser.decodePathF[IO, AppConfig]("heimdali"))

        httpEC <- ExecutionContexts.fixedThreadPool[IO](10)
        dbConnectionEC <- ExecutionContexts.fixedThreadPool[IO](10)
        dbTransactionEC <- ExecutionContexts.cachedThreadPool[IO]
        emailEC <- ExecutionContexts.fixedThreadPool[IO](10)
        provisionEC <- ExecutionContexts.fixedThreadPool[IO](config.provisioning.threadPoolSize)
        startupEC <- ExecutionContexts.fixedThreadPool[IO](1)

        h4Client = BlazeClientBuilder[IO](httpEC)
          .withRequestTimeout(5 minutes)
          .withResponseHeaderTimeout(5 minutes)
          .resource

        httpClient = new CMClient[IO](h4Client, config.cluster)
        clusterService = new CDHClusterService[IO](httpClient, config.cluster, hadoopConfiguration)

        loginContextProvider = new UGILoginContextProvider()
        sentryServiceClient = SentryGenericServiceClientFactory.create(hadoopConfiguration)
        sentryClient = new SentryClientImpl[IO](hiveTransactor, sentryServiceClient, loginContextProvider)
        hiveClient = new HiveClientImpl[IO](loginContextProvider, hiveTransactor)
        ldapClient = new LDAPClientImpl[IO](config.ldap) with ActiveDirectoryClient[IO]
        hdfsClient = new HDFSClientImpl[IO](hadoopConfiguration, loginContextProvider)
        yarnClient = new CDHYarnClient[IO](httpClient, config.cluster, clusterService)
        kafkaClient = new KafkaClientImpl[IO](config)
        emailClient = new EmailClientImpl[IO](config, emailEC)

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

        context = AppContext[IO](config, sentryClient, hiveClient, ldapClient, hdfsClient, yarnClient, kafkaClient, transactor, hiveDatabaseRepository, hiveGrantRepository, ldapRepository, memberRepository, yarnRepository, complianceRepository, workspaceRepository, topicRepository, topicGrantRepository, applicationRepository)

        configService = new DBConfigService[IO](config, configRepository, transactor)

        ldapGroupGenerator = LDAPGroupGenerator.instance(config, configService, _.ldapGroupGenerator)
        applicationGenerator = ApplicationGenerator.instance(config, ldapGroupGenerator, _.applicationGenerator)
        topicGenerator = TopicGenerator.instance(config, ldapGroupGenerator, _.topicGenerator)
        userTemplateGenerator = WorkspaceGenerator.instance[IO, WorkspaceGenerator[IO, UserTemplate]](config, ldapGroupGenerator, applicationGenerator, topicGenerator, _.userGenerator)
        simpleTemplateGenerator = WorkspaceGenerator.instance[IO, WorkspaceGenerator[IO, SimpleTemplate]](config, ldapGroupGenerator, applicationGenerator, topicGenerator, _.simpleGenerator)
        structuredTemplateGenerator = WorkspaceGenerator.instance[IO, WorkspaceGenerator[IO, StructuredTemplate]](config, ldapGroupGenerator, applicationGenerator, topicGenerator, _.structuredGenerator)

        workspaceService = new WorkspaceServiceImpl[IO](ldapClient, yarnRepository, hiveDatabaseRepository, ldapRepository, workspaceRepository, complianceRepository, approvalRepository, transactor, memberRepository, topicRepository, applicationRepository, context, null)
      } yield (workspaceService, ldapGroupGenerator)
  }

}
