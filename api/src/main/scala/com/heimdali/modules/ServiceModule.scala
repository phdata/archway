package com.heimdali.modules

import cats.effect.{ConcurrentEffect, Effect}
import com.heimdali.AppContext
import com.heimdali.models.{SimpleTemplate, StructuredTemplate, UserTemplate}
import com.heimdali.provisioning.DefaultProvisioningService
import com.heimdali.services._
import com.heimdali.templates.TemplateGenerator
import doobie.util.transactor.Transactor

trait ServiceModule[F[_]] {
  this: ExecutionContextModule[F]
    with ClientModule[F]
    with RepoModule
    with ConfigurationModule
    with HttpModule[F] =>

  def userTemplateGenerator: TemplateGenerator[F, UserTemplate] =
    TemplateGenerator.instance(appConfig, _.userGenerator)

  def simpleTemplateGenerator: TemplateGenerator[F, SimpleTemplate] =
    TemplateGenerator.instance(appConfig, _.simpleGenerator)

  def structuredTemplateGenerator: TemplateGenerator[F, StructuredTemplate] =
    TemplateGenerator.instance(appConfig, _.structuredGenerator)

  implicit def effect: ConcurrentEffect[F]

  private val metaTransactor = Transactor.fromDriverManager[F](
    appConfig.db.meta.driver,
    appConfig.db.meta.url,
    appConfig.db.meta.username.get,
    appConfig.db.meta.password.get
  )(effect, contextShift)

  val keytabService: KeytabService[F] =
    new KeytabServiceImpl[F]()

  val memberService: MemberService[F] =
    new MemberServiceImpl(
      memberRepository,
      metaTransactor,
      ldapRepository,
      ldapClient,
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

  val provisioningService: ProvisioningService[F] =
    new DefaultProvisioningService[F](reader)

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
      reader,
      provisioningService
    )

  val emailService: EmailService[F] =
    new EmailServiceImpl[F](emailClient, appConfig, workspaceService, ldapClient, templateEngine)

  val kafkaService: KafkaService[F] =
    new KafkaServiceImpl[F](reader)

  val applicationService: ApplicationService[F] =
    new ApplicationServiceImpl[F](reader)

  val accountService: AccountService[F] =
    new AccountServiceImpl[F](
      ldapClient,
      appConfig.rest,
      appConfig.approvers,
      appConfig.workspaces,
      workspaceService,
      userTemplateGenerator,
      provisioningService)
}
