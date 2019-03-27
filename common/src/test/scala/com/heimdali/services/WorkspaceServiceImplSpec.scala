package com.heimdali.services

import java.time.Instant

import cats.data.{EitherT, NonEmptyList, OptionT}
import cats.effect.{IO, Timer}
import cats.syntax.applicative._
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.provisioning.SimpleMessage
import com.heimdali.repositories.{MemberRepository, _}
import com.heimdali.test.fixtures.{id, _}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Strategy
import org.apache.hadoop.fs.Path
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class WorkspaceServiceImplSpec
  extends FlatSpec
    with Matchers
    with MockFactory {

  behavior of "Workspace Service"

  it should "list projects" in new Context {
    val invalidWorkspace = "CN=non_workspace,OU=groups,dn=example,dn=com"
    val validWorkspace = "CN=edh_sw_project,OU=groups,dn=example,dn=com"
    val memberships = Seq(
      invalidWorkspace,
      validWorkspace
    )

    workspaceRepository.list _ expects standardUsername returning List(searchResult).pure[ConnectionIO]

    val projects = projectServiceImpl.list(standardUsername).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(searchResult)
  }

  it should "create a workspace" in new Context {
    inSequence {
      (complianceRepository.create _).expects(initialCompliance).returning(savedCompliance.pure[ConnectionIO])
      (workspaceRepository.create _).expects(initialWorkspaceRequest.copy(compliance = savedCompliance)).returning(id.pure[ConnectionIO])

      (ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (grantRepository.create _).expects(id).returning(id.pure[ConnectionIO])
      (memberRepository.create _).expects(standardUserDN, id).returning(id.pure[ConnectionIO])

      // Read only
      (ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (grantRepository.create _).expects(id).returning(id.pure[ConnectionIO])

      (hiveDatabaseRepository.create _).expects(initialHive.copy(managingGroup = initialGrant.copy(id = Some(id), ldapRegistration = savedLDAP), readonlyGroup = Some(initialGrant.copy(id = Some(id), ldapRegistration = savedLDAP)))).returning(id.pure[ConnectionIO])
      (workspaceRepository.linkHive _).expects(id, id).returning(1.pure[ConnectionIO])

      (yarnRepository.create _).expects(initialYarn).returning(id.pure[ConnectionIO])
      (workspaceRepository.linkPool _).expects(id, id).returning(1.pure[ConnectionIO])

      (ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (applicationRepository.create _).expects(initialApplication.copy(group = savedLDAP)).returning(id.pure[ConnectionIO])
      (workspaceRepository.linkApplication _).expects(id, id).returning(1.pure[ConnectionIO])
      (memberRepository.create _).expects(standardUserDN, id).returning(1L.pure[ConnectionIO])
    }

    val newWorkspace =
      projectServiceImpl.create(initialWorkspaceRequest).unsafeRunSync()

    newWorkspace.processing should not be empty
    newWorkspace.data should not be empty
  }

  it should "find a record" in new Context {
    workspaceRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
    yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    approvalRepository.findByWorkspaceId _ expects id returning List(approval()).pure[ConnectionIO]
    topicRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO]
    applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO]

    val foundWorkspace = projectServiceImpl.find(id).value.unsafeRunSync()

    foundWorkspace shouldBe defined
    foundWorkspace.get.data should not be empty
    foundWorkspace.get.data.head.consumedInGB should not be defined
    foundWorkspace.get.processing should not be empty
  }

  it should "find a user workspace" in new Context {
    workspaceRepository.findByUsername _ expects standardUsername returning OptionT.some(savedWorkspaceRequest)
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
    yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    approvalRepository.findByWorkspaceId _ expects id returning List.empty[Approval].pure[ConnectionIO]
    topicRepository.findByWorkspaceId _ expects id returning List.empty[KafkaTopic].pure[ConnectionIO]
    applicationRepository.findByWorkspaceId _ expects id returning List.empty[Application].pure[ConnectionIO]

    val maybeWorkspace = projectServiceImpl.findByUsername(standardUsername).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest.copy(applications = List.empty))
  }

  it should "get consumed space if directory has been created" in new Context {
    val withCreated: HiveAllocation = savedHive.copy(directoryCreated = Some(timer.instant))

    workspaceRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(withCreated).pure[ConnectionIO]
    yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    approvalRepository.findByWorkspaceId _ expects id returning List(approval()).pure[ConnectionIO]
    topicRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO]
    applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO]
    hdfsClient.getConsumption _ expects withCreated.location returning IO.pure(1.0)

    val foundWorkspace = projectServiceImpl.find(id).value.unsafeRunSync()

    foundWorkspace shouldBe defined
    foundWorkspace.get.data should not be empty
    foundWorkspace.get.data.head.consumedInGB shouldBe Some(1.0)
    foundWorkspace.get.processing should not be empty
  }

  it should "approve the workspace" in new Context {
    val instant = Instant.now()
    approvalRepository.create _ expects(id, approval(instant)) returning approval(instant).copy(id = Some(id)).pure[ConnectionIO]

    workspaceRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
    yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    approvalRepository.findByWorkspaceId _ expects id returning List(approval()).pure[ConnectionIO]
    topicRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO]
    applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO]

    projectServiceImpl.approve(id, approval(instant)).unsafeRunSync()
  }

  it should "provision the workspace" in new Context {
    val instant = Instant.now()
    val firstApproval = approval(instant)
    val secondApproval = approval(instant)
    approvalRepository.create _ expects(id, firstApproval) returning firstApproval.copy(id = Some(id)).pure[ConnectionIO]
    approvalRepository.create _ expects(id, secondApproval) returning secondApproval.copy(id = Some(id)).pure[ConnectionIO]

    workspaceRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest) twice()
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO] twice()
    yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO] twice()
    topicRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO] twice()
    applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO] twice()

    approvalRepository.findByWorkspaceId _ expects id returning List(firstApproval).pure[ConnectionIO]
    approvalRepository.findByWorkspaceId _ expects id returning List(firstApproval, secondApproval).pure[ConnectionIO]

    (provisioningService.provision(_: WorkspaceRequest)) expects * returning NonEmptyList.one(SimpleMessage(Some(1l), "")).pure[IO]

    projectServiceImpl.approve(id, approval(instant)).unsafeRunSync()
    projectServiceImpl.approve(id, approval(instant)).unsafeRunSync()
  }

  it should "get all yarn applications" in new Context {
    yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    private val app = YarnApplication("application123", "MyApp")

    yarnClient.applications _ expects savedYarn.poolName returning List(app).pure[IO]

    val result = projectServiceImpl.yarnInfo(id).unsafeRunSync()

    result.head shouldBe YarnInfo(savedYarn.poolName, List(app))
  }

  it should "get details for hive" in new Context {
    val (request1, request2) = (savedHive.copy(name = "name1", databaseCreated = Some(timer.instant)), savedHive.copy(name = "name2", databaseCreated = Some(timer.instant)))
    hiveDatabaseRepository.findByWorkspace _ expects id returning List(request1, request2).pure[ConnectionIO]

    hiveClient.describeDatabase _ expects "name1" returning HiveDatabase("name1", List(HiveTable("table1"))).pure[IO]
    hiveClient.describeDatabase _ expects "name2" returning HiveDatabase("name2", List(HiveTable("table1"))).pure[IO]

    val maybeWorkspace = projectServiceImpl.hiveDetails(id).unsafeRunSync()

    maybeWorkspace shouldBe List(
      HiveDatabase("name1", List(HiveTable("table1"))),
      HiveDatabase("name2", List(HiveTable("table1")))
    )
  }

  it should "list risk workspaces" in new Context {
    workspaceRepository.pendingQueue _ expects Risk returning List(searchResult).pure[ConnectionIO]

    val projects = projectServiceImpl.reviewerList(Risk).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(searchResult)
  }

  it should "list infra workspaces" in new Context {
    workspaceRepository.pendingQueue _ expects Infra returning List(searchResult).pure[ConnectionIO]

    val projects = projectServiceImpl.reviewerList(Infra).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(searchResult)
  }

  trait Context {
    implicit val contextShift = IO.contextShift(ExecutionContext.global)

    val ldapClient: LDAPClient[IO] = mock[LDAPClient[IO]]
    val hdfsClient: HDFSClient[IO] = mock[HDFSClient[IO]]
    val sentryClient: SentryClient[IO] = mock[SentryClient[IO]]
    val hiveClient: HiveClient[IO] = mock[HiveClient[IO]]
    val yarnClient: YarnClient[IO] = mock[YarnClient[IO]]
    val kafkaClient: KafkaClient[IO] = mock[KafkaClient[IO]]
    val sentryRawClient: SentryGenericServiceClient = mock[SentryGenericServiceClient]

    val workspaceRepository: WorkspaceRequestRepository =
      mock[WorkspaceRequestRepository]
    val complianceRepository: ComplianceRepository = mock[ComplianceRepository]
    val yarnRepository: YarnRepository = mock[YarnRepository]
    val hiveDatabaseRepository: HiveAllocationRepository =
      mock[HiveAllocationRepository]
    val ldapRepository: LDAPRepository = mock[LDAPRepository]
    val approvalRepository: ApprovalRepository = mock[ApprovalRepository]
    val contextProvider: LoginContextProvider = mock[LoginContextProvider]
    val memberRepository: MemberRepository = mock[MemberRepository]
    val grantRepository: HiveGrantRepository = mock[HiveGrantRepository]
    val topicRepository: KafkaTopicRepository = mock[KafkaTopicRepository]
    val topicGrantRepository: TopicGrantRepository = mock[TopicGrantRepository]
    val applicationRepository: ApplicationRepository = mock[ApplicationRepository]

    val provisioningService: ProvisioningService[IO] = mock[ProvisioningService[IO]]

    val transactor = Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy(FC.unit, FC.unit, FC.unit, FC.unit))

    lazy val appConfig: AppContext[IO] = AppContext(
      null,
      sentryClient,
      hiveClient,
      ldapClient,
      hdfsClient,
      yarnClient,
      kafkaClient,
      transactor,
      hiveDatabaseRepository,
      grantRepository,
      ldapRepository,
      memberRepository,
      yarnRepository,
      complianceRepository,
      workspaceRepository,
      topicRepository,
      topicGrantRepository,
      applicationRepository)

    def projectServiceImpl =
      new WorkspaceServiceImpl[IO](
        ldapClient,
        yarnRepository,
        hiveDatabaseRepository,
        ldapRepository,
        workspaceRepository,
        complianceRepository,
        approvalRepository,
        transactor,
        memberRepository,
        topicRepository,
        applicationRepository,
        appConfig,
        provisioningService
      )
  }

}
