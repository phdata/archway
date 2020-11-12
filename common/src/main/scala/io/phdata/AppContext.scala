package io.phdata

import cats.effect._
import com.typesafe.config.{Config, ConfigFactory}
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import io.phdata
import io.phdata.caching.{CacheEntry, Cached}
import io.phdata.clients._
import io.phdata.config.AppConfig
import io.phdata.repositories._
import io.phdata.repositories.syntax.SqlSyntax
import io.phdata.services._
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.duration._

case class AppContext[F[_]](
    appConfig: AppConfig,
    clusterCache: Cached[F, Seq[Cluster]],
    roleClient: RoleClient[F],
    hiveClient: HiveClient[F],
    provisioningLDAPClient: ProvisioningLDAPClient[F],
    lookupLDAPClient: LookupLDAPClient[F],
    emailClient: EmailClient[F],
    featureService: FeatureService[F],
    transactor: Transactor[F],
    databaseRepository: HiveAllocationRepository,
    databaseGrantRepository: HiveGrantRepository,
    ldapRepository: LDAPRepository,
    memberRepository: MemberRepository,
    complianceRepository: ComplianceRepository,
    workspaceRequestRepository: WorkspaceRequestRepository,
    approvalRepository: ApprovalRepository,
    configRepository: ConfigRepository,
    complianceGroupRepository: ComplianceGroupRepository,
    complianceQuestionRepository: ComplianceQuestionRepository,
    customLinkGroupRepository: CustomLinkGroupRepository,
    customLinkRepository: CustomLinkRepository
)

object AppContext {

  def default[F[_]: ConcurrentEffect: ContextShift: Timer](
      config: Config = ConfigFactory.defaultApplication().resolve()
  ): Resource[F, AppContext[F]] =
    for {
      loadedConfig <- Resource.liftF(io.circe.config.parser.decodePathF[F, AppConfig](config, "archway"))
      config = loadedConfig

      httpEC <- ExecutionContexts.fixedThreadPool(10)
      dbConnectionEC <- ExecutionContexts.fixedThreadPool(10)
      dbTransactionEC <- ExecutionContexts.cachedThreadPool
      emailEC <- ExecutionContexts.fixedThreadPool(10)

      h4Client = BlazeClientBuilder[F](httpEC)
        .withRequestTimeout(5 minutes)
        .withResponseHeaderTimeout(5 minutes)
        .resource

      metaXA <- config.db.meta.tx(dbConnectionEC, dbTransactionEC)
      hiveXA = config.db.hive.hiveTx

      cacheService = new TimedCacheService()
      clusterCache <- Resource.liftF(cacheService.initial[F, Seq[Cluster]])
      _ <- Resource.liftF(clusterCache.put(CacheEntry(0L, Seq.empty)))

      featureService = new FeatureServiceImpl[F](config.featureFlags)

      roleClient = new RoleClientImpl[F](hiveXA)
      hiveClient = new HiveClientImpl[F](hiveXA)
      lookupLDAPClient = new LDAPClientImpl[F](
        config.ldap,
        config =>
          if (config.lookupBinding.server.isEmpty) {
            config.provisioningBinding
          } else {
            config.lookupBinding
          }
      )
      provisioningLDAPClient = new LDAPClientImpl[F](config.ldap, _.provisioningBinding)
      emailClient = new EmailClientImpl[F](config, emailEC)

      sqlSyntax = SqlSyntax(config.db.meta.driver)

      complianceRepository = new ComplianceRepositoryImpl
      ldapRepository = new LDAPRepositoryImpl
      hiveDatabaseRepository = new HiveAllocationRepositoryImpl
      workspaceRepository = new WorkspaceRequestRepositoryImpl(sqlSyntax)
      approvalRepository = new ApprovalRepositoryImpl
      memberRepository = new MemberRepositoryImpl(sqlSyntax)
      hiveGrantRepository = new HiveGrantRepositoryImpl
      configRepository = new ConfigRepositoryImpl
      complianceGroupRepository = new ComplianceGroupRepositoryImpl
      complianceQuestionRepository = new ComplianceQuestionRepositoryImpl
      customLinkGroupRepository = new CustomLinkGroupRepositoryImpl
      customLinkRepository = new CustomLinkRepositoryImpl
    } yield phdata.AppContext[F](
      config,
      clusterCache,
      roleClient,
      hiveClient,
      provisioningLDAPClient,
      lookupLDAPClient,
      emailClient,
      featureService,
      metaXA,
      hiveDatabaseRepository,
      hiveGrantRepository,
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
