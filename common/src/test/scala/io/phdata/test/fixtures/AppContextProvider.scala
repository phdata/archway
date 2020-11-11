package io.phdata.test.fixtures

import cats.effect.concurrent.MVar
import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.caching.{CacheEntry, Cached}
import io.phdata.clients._
import io.phdata.config.{AppConfig, AvailableFeatures}
import io.phdata.services._
import doobie.util.transactor.Strategy
import doobie.{FC, Transactor}
import io.phdata.repositories._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

trait AppContextProvider {
  this: FlatSpec with MockFactory =>


  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val emptyTransactor = Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy(FC.unit, FC.unit, FC.unit, FC.unit))

  def genMockContext(clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].empty[CacheEntry[Seq[Cluster]]].unsafeRunSync(),
                     transactor: Transactor[IO] = emptyTransactor,
                     appConfig: AppConfig = appConfig,
                     provisioningLDAPClient: ProvisioningLDAPClient[IO] = mock[ProvisioningLDAPClient[IO]],
                     lookupLDAPClient: LookupLDAPClient[IO] = mock[LookupLDAPClient[IO]],
                     roleClient: RoleClient[IO] = mock[RoleClient[IO]],
                     hiveClient: HiveClient[IO] = mock[HiveClient[IO]],
                     emailClient: EmailClient[IO] = mock[EmailClient[IO]],
                     clusterService: ClusterService[IO] = mock[ClusterService[IO]],
                     featureService: FeatureService[IO] = new FeatureServiceImpl[IO](AvailableFeatures.all),
                     workspaceRepository: WorkspaceRequestRepository = mock[WorkspaceRequestRepository],
                     complianceRepository: ComplianceRepository = mock[ComplianceRepository],
                     hiveDatabaseRepository: HiveAllocationRepository = mock[HiveAllocationRepository],
                     ldapRepository: LDAPRepository = mock[LDAPRepository],
                     memberRepository: MemberRepository = mock[MemberRepository],
                     grantRepository: HiveGrantRepository = mock[HiveGrantRepository],
                     provisioningService: ProvisioningService[IO] = mock[ProvisioningService[IO]],
                     approvalRepository: ApprovalRepository = mock[ApprovalRepository],
                     configRepository: ConfigRepository = mock[ConfigRepository],
                     complianceGroupRepository: ComplianceGroupRepository = mock[ComplianceGroupRepository],
                     complianceQuestionRepository: ComplianceQuestionRepository = mock[ComplianceQuestionRepository],
                     customLinkGroupRepository: CustomLinkGroupRepository = mock[CustomLinkGroupRepository],
                     customLinkRepository: CustomLinkRepository = mock[CustomLinkRepository]
                    ): AppContext[IO] =
    AppContext(
      appConfig,
      clusterCache,
      roleClient,
      hiveClient,
      provisioningLDAPClient,
      lookupLDAPClient,
      emailClient,
      clusterService,
      featureService,
      transactor,
      hiveDatabaseRepository,
      grantRepository,
      ldapRepository,
      memberRepository,
      complianceRepository,
      workspaceRepository,
      approvalRepository,
      configRepository,
      complianceGroupRepository,
      complianceQuestionRepository,
      customLinkGroupRepository,
      customLinkRepository
    )
}
