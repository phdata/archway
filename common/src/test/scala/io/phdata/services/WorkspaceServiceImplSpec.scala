package io.phdata.services

import java.time.Instant

import cats.data.{NonEmptyList, OptionT}
import cats.effect.{IO, Timer}
import cats.syntax.applicative._
import io.phdata.AppContext
import io.phdata.models._
import io.phdata.provisioning.{Message, SimpleMessage}
import io.phdata.repositories._
import io.phdata.test.fixtures.{id, _}
import doobie._
import doobie.implicits._
import io.phdata.provisioning.Message
import io.phdata.repositories.WorkspaceRequestRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class WorkspaceServiceImplSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with AppContextProvider {

  behavior of "Workspace Service"

  it should "list projects" in new Context {
    context.workspaceRequestRepository.list _ expects standardUserDN returning List(searchResult).pure[ConnectionIO]

    val projects = workspaceServiceImpl.list(standardUserDN).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(searchResult)
  }

  it should "userAccessible returns true if a user has access to a workspace" in new Context {
    val userWorkspace = 1L
    context.workspaceRequestRepository.userAccessible _ expects standardUserDN returning List(userWorkspace).pure[ConnectionIO]
    val hasAccess = workspaceServiceImpl.userAccessible(standardUserDN, userWorkspace).unsafeRunSync()
    assert(hasAccess)
  }

  it should "userAccessible returns false if a user does not access to a workspace" in new Context {
    val workspaceId = 1L
    val noAccessWorkspace = 2L
    context.workspaceRequestRepository.userAccessible _ expects standardUserDN returning List(workspaceId).pure[ConnectionIO]
    val hasAccess = workspaceServiceImpl.userAccessible(standardUserDN, noAccessWorkspace).unsafeRunSync()
    assert(!hasAccess)
  }

  it should "create a workspace" in new Context {
    inSequence {
      (context.complianceRepository.create _).expects(initialCompliance).returning(savedCompliance.pure[ConnectionIO])
      (context.workspaceRequestRepository.create _).expects(initialWorkspaceRequest.copy(compliance = savedCompliance)).returning(id.pure[ConnectionIO])

      (context.ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (context.databaseGrantRepository.create _).expects(id).returning(id.pure[ConnectionIO])
      (context.memberRepository.create _).expects(standardUserDN, id).returning(id.pure[ConnectionIO])

      // Read only
      (context.ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (context.databaseGrantRepository.create _).expects(id).returning(id.pure[ConnectionIO])

      (context.databaseRepository.create _).expects(initialHive.copy(managingGroup = initialGrant.copy(id = Some(id), ldapRegistration = savedLDAP), readonlyGroup = Some(initialGrant.copy(id = Some(id), ldapRegistration = savedLDAP)))).returning(id.pure[ConnectionIO])
      (context.workspaceRequestRepository.linkHive _).expects(id, id).returning(1.pure[ConnectionIO])

      (context.yarnRepository.create _).expects(initialYarn).returning(id.pure[ConnectionIO])
      (context.workspaceRequestRepository.linkPool _).expects(id, id).returning(1.pure[ConnectionIO])

      (context.ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (context.applicationRepository.create _).expects(initialApplication.copy(group = savedLDAP)).returning(id.pure[ConnectionIO])
      (context.workspaceRequestRepository.linkApplication _).expects(id, id).returning(1.pure[ConnectionIO])
      (context.memberRepository.create _).expects(standardUserDN, id).returning(1L.pure[ConnectionIO])
      (context.workspaceRequestRepository.find _).expects(123).returning(OptionT.some(savedWorkspaceRequest))
      (context.databaseRepository.findByWorkspace _).expects(id)returning List(savedHive).pure[ConnectionIO]
      (context.yarnRepository.findByWorkspaceId _).expects(id)returning List(savedYarn).pure[ConnectionIO]
      (context.approvalRepository.findByWorkspaceId _).expects(id)returning List(approval()).pure[ConnectionIO]
      (context.kafkaRepository.findByWorkspaceId _).expects(id)returning List(savedTopic).pure[ConnectionIO]
      (context.applicationRepository.findByWorkspaceId _).expects(id).returning(List(savedApplication).pure[ConnectionIO])
    }

    val newWorkspace = workspaceServiceImpl.create(initialWorkspaceRequest).unsafeRunSync()

    newWorkspace.processing should not be empty
    newWorkspace.data should not be empty
  }

  it should "find a record" in new Context {
    context.workspaceRequestRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    context.databaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
    context.yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    context.approvalRepository.findByWorkspaceId _ expects id returning List(approval()).pure[ConnectionIO]
    context.kafkaRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO]
    context.applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO]

    val foundWorkspace = workspaceServiceImpl.find(id).value.unsafeRunSync()

    foundWorkspace shouldBe defined
    foundWorkspace.get.data should not be empty
    foundWorkspace.get.data.head.consumedInGB should not be defined
    foundWorkspace.get.processing should not be empty
  }

  it should "find a user workspace" in new Context {
    context.workspaceRequestRepository.findByUsername _ expects standardUserDN returning OptionT.some(savedWorkspaceRequest)
    context.databaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
    context.yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    context.approvalRepository.findByWorkspaceId _ expects id returning List.empty[Approval].pure[ConnectionIO]
    context.kafkaRepository.findByWorkspaceId _ expects id returning List.empty[KafkaTopic].pure[ConnectionIO]
    context.applicationRepository.findByWorkspaceId _ expects id returning List.empty[Application].pure[ConnectionIO]

    val maybeWorkspace = workspaceServiceImpl.findByUsername(standardUserDN).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest.copy(applications = List.empty))
  }

  it should "get consumed space if directory has been created" in new Context {
    val withCreated: HiveAllocation = savedHive.copy(directoryCreated = Some(testTimer.instant))

    context.workspaceRequestRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    context.databaseRepository.findByWorkspace _ expects id returning List(withCreated).pure[ConnectionIO]
    context.yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    context.approvalRepository.findByWorkspaceId _ expects id returning List(approval()).pure[ConnectionIO]
    context.kafkaRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO]
    context.applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO]
    context.hdfsClient.getConsumption _ expects withCreated.location returning IO.pure(1.0)

    val foundWorkspace = workspaceServiceImpl.find(id).value.unsafeRunSync()

    foundWorkspace shouldBe defined
    foundWorkspace.get.data should not be empty
    foundWorkspace.get.data.head.consumedInGB shouldBe Some(1.0)
    foundWorkspace.get.processing should not be empty
  }

  it should "approve the workspace" in new Context {
    val instant = Instant.now()
    context.approvalRepository.create _ expects(id, approval(instant)) returning approval(instant).copy(id = Some(id)).pure[ConnectionIO]

    context.workspaceRequestRepository.find _ expects id returning OptionT.some(savedWorkspaceRequest)
    context.databaseRepository.findByWorkspace _ expects id returning List(savedHive).pure[ConnectionIO]
    context.yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    context.approvalRepository.findByWorkspaceId _ expects id returning List(approval()).pure[ConnectionIO]
    context.kafkaRepository.findByWorkspaceId _ expects id returning List(savedTopic).pure[ConnectionIO]
    context.applicationRepository.findByWorkspaceId _ expects id returning List(savedApplication).pure[ConnectionIO]

    (provisioningService.attemptProvision(_: WorkspaceRequest, _: Int)) expects(*, 2) returning NonEmptyList.one(SimpleMessage(1l, "").asInstanceOf[Message]).pure[IO].start(contextShift)

    workspaceServiceImpl.approve(id, approval(instant)).unsafeRunSync()
  }

  it should "get a workspace status when not provisioned" in new Context {
    provisioningService.findUnprovisioned _ expects () returning List(savedWorkspaceRequest).pure[IO]
    val result = workspaceServiceImpl.status(id).unsafeRunSync()
    result shouldBe WorkspaceStatus(WorkspaceProvisioningStatus.PENDING)
  }

  it should "get a workspace status when provisioned" in new Context {
    val unknownId = 456L
    provisioningService.findUnprovisioned _ expects () returning List(savedWorkspaceRequest).pure[IO]
    val result = workspaceServiceImpl.status(unknownId).unsafeRunSync()
    result shouldBe WorkspaceStatus(WorkspaceProvisioningStatus.COMPLETED)
  }

  it should "get all yarn applications" in new Context {
    context.yarnRepository.findByWorkspaceId _ expects id returning List(savedYarn).pure[ConnectionIO]
    private val app = YarnApplication("application123", "MyApp")

    context.yarnClient.applications _ expects savedYarn.poolName returning List(app).pure[IO]

    val result = workspaceServiceImpl.yarnInfo(id).unsafeRunSync()

    result.head shouldBe YarnInfo(savedYarn.poolName, List(app))
  }

  it should "get details for hive" in new Context {
    val (request1, request2) = (savedHive.copy(name = "name1", databaseCreated = Some(testTimer.instant)), savedHive.copy(name = "name2", databaseCreated = Some(testTimer.instant)))
    context.databaseRepository.findByWorkspace _ expects id returning List(request1, request2).pure[ConnectionIO]

    context.hiveClient.describeDatabase _ expects "name1" returning HiveDatabase("name1", List(HiveTable("table1"))).pure[IO]
    context.hiveClient.describeDatabase _ expects "name2" returning HiveDatabase("name2", List(HiveTable("table1"))).pure[IO]

    val maybeWorkspace = workspaceServiceImpl.hiveDetails(id).unsafeRunSync()

    maybeWorkspace shouldBe List(
      HiveDatabase("name1", List(HiveTable("table1"))),
      HiveDatabase("name2", List(HiveTable("table1")))
    )
  }

  it should "list risk workspaces" in new Context {
    context.workspaceRequestRepository.pendingQueue _ expects Risk returning List(searchResult).pure[ConnectionIO]

    val projects = workspaceServiceImpl.reviewerList(Risk).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(searchResult)
  }

  it should "list infra workspaces" in new Context {
    context.workspaceRequestRepository.pendingQueue _ expects Infra returning List(searchResult).pure[ConnectionIO]

    val projects = workspaceServiceImpl.reviewerList(Infra).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(searchResult)
  }

  trait Context {
    implicit val timer: Timer[IO] = testTimer

    val provisioningService: ProvisioningService[IO] = mock[ProvisioningService[IO]]
    val context: AppContext[IO] = genMockContext()

    lazy val workspaceServiceImpl = new WorkspaceServiceImpl[IO](provisioningService, context)
  }

}
