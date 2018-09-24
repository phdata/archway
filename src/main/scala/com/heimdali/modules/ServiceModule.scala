package com.heimdali.modules

import com.heimdali.models.AppContext
import com.heimdali.services._
import doobie.util.transactor.Transactor

trait ServiceModule[F[_]] {
  this: AppModule[F]
    with ContextModule[F]
    with ExecutionContextModule
    with ClientModule[F]
    with RepoModule
    with ConfigurationModule
    with HttpModule[F] =>

  private val metaTransactor = Transactor.fromDriverManager[F](
    appConfig.db.meta.driver,
    appConfig.db.meta.url,
    appConfig.db.meta.username.get,
    appConfig.db.meta.password.get
  )

  val keytabService: KeytabService[F] =
    new KeytabServiceImpl[F]()

  val environment: String =
    configuration.getString("cluster.environment")

  val memberService: MemberService[F] =
    new MemberServiceImpl(
      memberRepository,
      metaTransactor,
      ldapRepository,
      ldapClient
    )

  val reader: AppContext[F] = AppContext[F](
    appConfig,
    sentryClient,
    hiveClient,
    ldapClient,
    hdfsClient,
    yarnClient,
    kafkaClient,
    metaTransactor,
    hiveDatabaseRepository,
    hiveGrantRepository,
    ldapRepository,
    memberRepository,
    yarnRepository,
    complianceRepository,
    workspaceRepository,
    topicRepository,
    topicGrantRepository,
    applicationRepository)

  val workspaceService: WorkspaceService[F] =
    new WorkspaceServiceImpl[F](
      ldapClient,
      yarnRepository,
      hiveDatabaseRepository,
      ldapRepository,
      workspaceRepository,
      complianceRepository,
      approvalRepository,
      metaTransactor,
      memberRepository,
      topicRepository,
      applicationRepository,
      reader
    )

  val kafkaService: KafkaService[F] =
    new KafkaServiceImpl[F](reader)

  val applicationService: ApplicationService[F] =
    new ApplicationServiceImpl[F](reader)

  val accountService: AccountService[F] =
    new AccountServiceImpl[F](ldapClient, appConfig.rest, appConfig.approvers, appConfig.workspaces, workspaceService)
}
