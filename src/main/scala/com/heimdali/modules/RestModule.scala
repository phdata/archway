package com.heimdali.modules

import cats.effect.IO
import com.heimdali.rest._
import java.time.Clock
import com.heimdali.services.KafkaService

trait RestModule {
  this: ServiceModule[IO]
    with HttpModule[IO]
    with ExecutionContextModule[IO]
    with ConfigurationModule
    with ClusterModule[IO] =>

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
      clock
    )
  val templateController = new TemplateController(authService)

  val memberController: MemberController = new MemberController(authService, memberService)

  val riskController: RiskController = new RiskController(authService, workspaceService)

  val restAPI: RestAPI = new RestAPI(
    accountController,
    clusterController,
    workspaceController,
    templateController,
    memberController,
    riskController,
  )
}
