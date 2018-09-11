package com.heimdali.services

import java.time.Instant

import cats.data.{EitherT, OptionT}
import cats.effect.IO
import cats.syntax.applicative._
import com.heimdali.clients._
import com.heimdali.models.{AppContext, WorkspaceMember}
import com.heimdali.repositories.{MemberRepository, _}
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.apache.hadoop.fs.Path
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class WorkspaceServiceImplSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with DBTest {

  behavior of "Workspace Service"

  it should "list projects" in new Context {
    val invalidWorkspace = "CN=non_workspace,OU=groups,dn=example,dn=com"
    val validWorkspace = "CN=edh_sw_project,OU=groups,dn=example,dn=com"
    val memberships = Seq(
      invalidWorkspace,
      validWorkspace
    )

    workspaceRepository.list _ expects standardUsername returning List(
      savedWorkspaceRequest
    ).pure[ConnectionIO]

    val projects = projectServiceImpl.list(standardUsername).unsafeRunSync()
    projects.length should be(1)
    projects.head should be(savedWorkspaceRequest)
  }

  it should "get memberships accurately" in new Context {
    val table: TableFor2[LDAPUser, Seq[String]] = Table(
      ("user", "memberships"),
      (LDAPUser("name", "username", Seq("something_else")), Seq.empty)
    )

    forAll(table) { (user, memberships) =>
      projectServiceImpl.sharedMemberships(user) should be(memberships)
    }
  }

  it should "create a workspace" in new Context {
    inSequence {
      (complianceRepository.create _).expects(initialCompliance).returning(savedCompliance.pure[ConnectionIO])
      (workspaceRepository.create _).expects(initialWorkspaceRequest.copy(compliance = savedCompliance)).returning(id.pure[ConnectionIO])
      (ldapRepository.create _).expects(initialLDAP).returning(savedLDAP.pure[ConnectionIO])
      (grantRepository.create _).expects(id).returning(id.pure[ConnectionIO])
      (memberRepository.create _).expects(standardUsername, id).returning(id.pure[ConnectionIO])
      (hiveDatabaseRepository.create _).expects(initialHive.copy(managingGroup = initialGrant.copy(id = Some(id), ldapRegistration = savedLDAP))).returning(id.pure[ConnectionIO])
      (workspaceRepository.linkHive _).expects(id, id).returning(1.pure[ConnectionIO])

      (yarnRepository.create _).expects(initialYarn).returning(id.pure[ConnectionIO])
      (workspaceRepository.linkPool _).expects(id, id).returning(1.pure[ConnectionIO])
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
    foundWorkspace.get.processing should not be empty
  }

  it should "find a user workspace" in new Context {
    workspaceRepository.findByUsername _ expects standardUsername returning OptionT.some(savedWorkspaceRequest)

    val maybeWorkspace = projectServiceImpl.findByUsername(standardUsername).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  it should "approve the workspace" in new Context {
    val instant = Instant.now()
    approvalRepository.create _ expects(id, approval(instant)) returning approval(
      instant
    ).copy(id = Some(id)).pure[ConnectionIO]

    projectServiceImpl.approve(id, approval(instant))
  }

  it should "provision a workspace" in new Context {
    inSequence {
      hdfsClient.createDirectory _ expects(savedHive.location, None) returning IO
        .pure(new Path(savedHive.location))
      hiveDatabaseRepository.directoryCreated _ expects id returning 0.pure[ConnectionIO]
      hdfsClient.setQuota _ expects(savedHive.location, savedHive.sizeInGB) returning IO
        .pure(HDFSAllocation(savedHive.location, savedHive.sizeInGB))
      hiveDatabaseRepository.quotaSet _ expects id returning 0.pure[ConnectionIO]
      hiveClient.createDatabase _ expects(savedHive.name, savedHive.location) returning IO.unit
      hiveDatabaseRepository.databaseCreated _ expects id returning 0.pure[ConnectionIO]

      ldapClient.createGroup _ expects(savedLDAP.id.get, savedLDAP.commonName, savedLDAP.distinguishedName) returning EitherT
        .right(IO.unit)
      ldapRepository.groupCreated _ expects id returning 0.pure[ConnectionIO]
      hiveClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
      ldapRepository.roleCreated _ expects id returning 0.pure[ConnectionIO]
      hiveClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
      ldapRepository.groupAssociated _ expects id returning 0.pure[ConnectionIO]
      hiveClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.sentryRole) returning IO.unit
      grantRepository.databaseGranted _ expects id returning 0.pure[ConnectionIO]
      hiveClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.sentryRole) returning IO.unit
      grantRepository.locationGranted _ expects id returning 0.pure[ConnectionIO]
    }

    inSequence {
      ldapClient.addUser _ expects(savedLDAP.distinguishedName, standardUsername) returning OptionT
        .some(LDAPUser("John Doe", standardUsername, Seq.empty))
      memberRepository.complete _ expects(id, standardUsername) returning 0.pure[ConnectionIO]
    }


    inSequence {
      yarnClient.createPool _ expects(poolName, maxCores, maxMemoryInGB) returning IO.unit
      yarnRepository.complete _ expects id returning 0.pure[ConnectionIO]
    }

    projectServiceImpl.provision(savedWorkspaceRequest).unsafeRunSync()
  }

  trait Context {
    val ldapClient: LDAPClient[IO] = mock[LDAPClient[IO]]
    val hdfsClient: HDFSClient[IO] = mock[HDFSClient[IO]]
    val hiveClient: SentryClient[IO] = mock[SentryClient[IO]]
    val yarnClient: YarnClient[IO] = mock[YarnClient[IO]]
    val kafkaClient: KafkaClient[IO] = mock[KafkaClient[IO]]
    val sentryClient: SentryGenericServiceClient = mock[SentryGenericServiceClient]

    val workspaceRepository: WorkspaceRequestRepository =
      mock[WorkspaceRequestRepository]
    val complianceRepository: ComplianceRepository = mock[ComplianceRepository]
    val yarnRepository: YarnRepository = mock[YarnRepository]
    val hiveDatabaseRepository: HiveDatabaseRepository =
      mock[HiveDatabaseRepository]
    val ldapRepository: LDAPRepository = mock[LDAPRepository]
    val approvalRepository: ApprovalRepository = mock[ApprovalRepository]
    val contextProvider: LoginContextProvider = mock[LoginContextProvider]
    val memberRepository: MemberRepository = mock[MemberRepository]
    val grantRepository: HiveGrantRepository = mock[HiveGrantRepository]
    val topicRepository: KafkaTopicRepository = mock[KafkaTopicRepository]
    val topicGrantRepository: TopicGrantRepository = mock[TopicGrantRepository]
    val applicationRepository: ApplicationRepository = mock[ApplicationRepository]

    lazy val appConfig: AppContext[IO] = AppContext(
      null,
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
        appConfig
      )
  }

}
