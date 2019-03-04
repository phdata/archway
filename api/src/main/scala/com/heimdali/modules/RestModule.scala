package com.heimdali.modules

import cats.effect.IO
import com.heimdali.rest._
import java.time.Clock
import com.heimdali.services.KafkaService

trait RestModule {
  this: ServiceModule[IO]
    with ClientModule[IO]
    with HttpModule[IO]
    with ExecutionContextModule[IO]
    with ConfigurationModule =>

  val authService: AuthServiceImpl[IO] =
    new AuthServiceImpl[IO](accountService)

  val accountController =
    new AccountController(authService, accountService)

  val clusterController =
    new ClusterController(clusterService)

  val workspaceController =
    new WorkspaceController(
      authService,
      workspaceService,
      memberService,
      kafkaService,
      applicationService,
      emailService,
      provisioningService,
      clock,
    )
  val templateController =
    new TemplateController(authService, simpleTemplateGenerator, structuredTemplateGenerator)

  val memberController: MemberController =
    new MemberController(authService, memberService)

  val riskController: RiskController =
    new RiskController(authService, workspaceService)

  val opsController: OpsController =
    new OpsController(authService, workspaceService)

  val restAPI: RestAPI = new RestAPI(
    accountController,
    clusterController,
    workspaceController,
    templateController,
    memberController,
    riskController,
    opsController,
    appConfig
  )
}
