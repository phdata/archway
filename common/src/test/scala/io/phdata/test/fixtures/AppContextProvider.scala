package io.phdata.test.fixtures

import cats.effect.concurrent.MVar
import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.caching.{CacheEntry, Cached}
import io.phdata.clients._
import io.phdata.config.AppConfig
import io.phdata.repositories.{ApplicationRepository, ApprovalRepository, ComplianceRepository, ConfigRepository, HiveAllocationRepository, HiveGrantRepository, KafkaTopicRepository, LDAPRepository, MemberRepository, TopicGrantRepository, WorkspaceRequestRepository, YarnRepository}
import io.phdata.services.{Cluster, ClusterService, FeatureService, LoginContextProvider, ProvisioningService}
import doobie.util.transactor.Strategy
import doobie.{FC, Transactor}
import io.phdata.repositories.{ApplicationRepository, ApprovalRepository, ComplianceRepository, ConfigRepository, HiveAllocationRepository, HiveGrantRepository, KafkaTopicRepository, LDAPRepository, MemberRepository, TopicGrantRepository, WorkspaceRequestRepository, YarnRepository}
import io.phdata.services.{FeatureService, ProvisioningService}
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

trait AppContextProvider {
  this: FlatSpec with MockFactory =>

  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val emptyTransactor = Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy(FC.unit, FC.unit, FC.unit, FC.unit))

  def genMockContext(clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].empty[CacheEntry[Seq[Cluster]]].unsafeRunSync(),
                     loginContextProvider: LoginContextProvider = mock[LoginContextProvider],
                     transactor: Transactor[IO] = emptyTransactor,
                     appConfig: AppConfig = appConfig,
                     provisioningLDAPClient: LDAPClient[IO] = mock[LDAPClient[IO]],
                     lookupLDAPClient: LDAPClient[IO] = mock[LDAPClient[IO]],
                     hdfsClient: HDFSClient[IO] = mock[HDFSClient[IO]],
                     sentryClient: SentryClient[IO] = mock[SentryClient[IO]],
                     hiveClient: HiveClient[IO] = mock[HiveClient[IO]],
                     impalaClient: ImpalaClient[IO] = mock[ImpalaClient[IO]],
                     yarnClient: YarnClient[IO] = mock[YarnClient[IO]],
                     kafkaClient: KafkaClient[IO] = mock[KafkaClient[IO]],
                     emailClient: EmailClient[IO] = mock[EmailClient[IO]],
                     kerberosClient: KerberosClient[IO] = mock[KerberosClient[IO]],
                     clusterService: ClusterService[IO] = mock[ClusterService[IO]],
                     featureService: FeatureService[IO] = mock[FeatureService[IO]],
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
                     approvalRepository: ApprovalRepository = mock[ApprovalRepository],
                     configRepository: ConfigRepository = mock[ConfigRepository]): AppContext[IO] =
    AppContext(
      appConfig,
      clusterCache,
      loginContextProvider,
      sentryClient,
      hiveClient,
      Some(impalaClient),
      provisioningLDAPClient,
      lookupLDAPClient,
      hdfsClient,
      yarnClient,
      kafkaClient,
      emailClient,
      kerberosClient,
      clusterService,
      featureService,
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
      approvalRepository,
      configRepository)
}