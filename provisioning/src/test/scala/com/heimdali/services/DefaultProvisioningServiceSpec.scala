package com.heimdali.services

import cats.data.OptionT
import cats.effect.{IO, Timer}
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.models._
import com.heimdali.test.fixtures._
import com.heimdali.provisioning.DefaultProvisioningService
import com.heimdali.repositories.{MemberRepository, _}
import com.heimdali.test.fixtures.{TestTimer, id, maxCores, maxMemoryInGB, poolName, savedHive, savedLDAP, savedWorkspaceRequest, standardUserDN}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Strategy
import org.apache.hadoop.fs.Path
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class DefaultProvisioningServiceSpec extends FlatSpec with MockFactory with Matchers {

  behavior of "DefaultProvisioningServiceSpec"

  it should "provision a workspace" in new Context {
    inSequence {
      hdfsClient.createDirectory _ expects(savedHive.location, None) returning IO
        .pure(new Path(savedHive.location))
      hiveDatabaseRepository.directoryCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      hdfsClient.setQuota _ expects(savedHive.location, savedHive.sizeInGB) returning IO
        .pure(HDFSAllocation(savedHive.location, savedHive.sizeInGB))
      hiveDatabaseRepository.quotaSet _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      hiveClient.createDatabase _ expects(savedHive.name, savedHive.location) returning IO.unit
      hiveDatabaseRepository.databaseCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]

      ldapClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
      ldapRepository.groupCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
      ldapRepository.roleCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
      ldapRepository.groupAssociated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.sentryRole, Manager) returning IO.unit
      grantRepository.databaseGranted _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.sentryRole) returning IO.unit
      grantRepository.locationGranted _ expects(id, timer.instant) returning 0.pure[ConnectionIO]

      ldapClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
      ldapRepository.groupCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
      ldapRepository.roleCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
      ldapRepository.groupAssociated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.enableAccessToDB _ expects(savedHive.name, savedLDAP.sentryRole, ReadOnly) returning IO.unit
      grantRepository.databaseGranted _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.enableAccessToLocation _ expects(savedHive.location, savedLDAP.sentryRole) returning IO.unit
      grantRepository.locationGranted _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
    }

    inSequence {
      ldapClient.addUser _ expects(savedLDAP.distinguishedName, standardUserDN) returning OptionT.some(standardUserDN)
      memberRepository.complete _ expects(id, standardUserDN) returning 0.pure[ConnectionIO]
    }

    inSequence {
      yarnClient.createPool _ expects(poolName, maxCores, maxMemoryInGB) returning IO.unit
      yarnRepository.complete _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
    }

    inSequence {
      ldapClient.createGroup _ expects(savedLDAP.commonName, *) returning IO.unit
      ldapRepository.groupCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit
      ldapRepository.roleCreated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit
      ldapRepository.groupAssociated _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
      sentryClient.grantPrivilege _ expects(*, *, *) returning IO.unit
      applicationRepository.consumerGroupAccess _ expects(id, timer.instant) returning 0.pure[ConnectionIO]
    }

    inSequence {
      ldapClient.addUser _ expects(savedLDAP.distinguishedName, standardUserDN) returning OptionT.some(standardUserDN)
      memberRepository.complete _ expects(id, standardUserDN) returning 0.pure[ConnectionIO]
    }

    provisioningService.provision(savedWorkspaceRequest).unsafeRunSync().map(x => println(x.message))
  }

  trait Context {
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

    implicit val cs = IO.contextShift(ExecutionContext.global)
    val transactor = Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy(FC.unit, FC.unit, FC.unit, FC.unit))

    lazy val appContext: AppContext[IO] = AppContext(
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

    lazy val provisioningService = new DefaultProvisioningService[IO](appContext)
  }

}
