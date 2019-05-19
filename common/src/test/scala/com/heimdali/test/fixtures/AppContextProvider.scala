package com.heimdali.test.fixtures

import cats.effect.{ContextShift, IO}
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.config.AppConfig
import com.heimdali.repositories.{ApplicationRepository, ApprovalRepository, ComplianceRepository, HiveAllocationRepository, HiveGrantRepository, KafkaTopicRepository, LDAPRepository, MemberRepository, TopicGrantRepository, WorkspaceRequestRepository, YarnRepository}
import com.heimdali.services.{LoginContextProvider, ProvisioningService}
import doobie.util.transactor.Strategy
import doobie.{FC, Transactor}
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

trait AppContextProvider {
  this: FlatSpec with MockFactory =>

  implicit def contextShift: ContextShift[IO]

  val emptyTransactor = Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy(FC.unit, FC.unit, FC.unit, FC.unit))

  def genMockContext(transactor: Transactor[IO] = emptyTransactor,
                     appConfig: AppConfig = appConfig,
                     ldapClient: LDAPClient[IO] = mock[LDAPClient[IO]],
                     hdfsClient: HDFSClient[IO] = mock[HDFSClient[IO]],
                     sentryClient: SentryClient[IO] = mock[SentryClient[IO]],
                     hiveClient: HiveClient[IO] = mock[HiveClient[IO]],
                     yarnClient: YarnClient[IO] = mock[YarnClient[IO]],
                     kafkaClient: KafkaClient[IO] = mock[KafkaClient[IO]],
                     sentryRawClient: SentryGenericServiceClient = mock[SentryGenericServiceClient],
                     workspaceRepository: WorkspaceRequestRepository = mock[WorkspaceRequestRepository],
                     complianceRepository: ComplianceRepository = mock[ComplianceRepository],
                     yarnRepository: YarnRepository = mock[YarnRepository],
                     hiveDatabaseRepository: HiveAllocationRepository = mock[HiveAllocationRepository],
                     ldapRepository: LDAPRepository = mock[LDAPRepository],
                     contextProvider: LoginContextProvider = mock[LoginContextProvider],
                     memberRepository: MemberRepository = mock[MemberRepository],
                     grantRepository: HiveGrantRepository = mock[HiveGrantRepository],
                     topicRepository: KafkaTopicRepository = mock[KafkaTopicRepository],
                     topicGrantRepository: TopicGrantRepository = mock[TopicGrantRepository],
                     applicationRepository: ApplicationRepository = mock[ApplicationRepository],
                     provisioningService: ProvisioningService[IO] = mock[ProvisioningService[IO]],
                     approvalRepository: ApprovalRepository = mock[ApprovalRepository]): AppContext[IO] =
    AppContext(
      appConfig,
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
      applicationRepository,
      approvalRepository)
}
