package com.heimdali.modules

import cats.effect.IO
import com.heimdali.rest._
import java.time.Clock

trait RestModule {
  this: ServiceModule[IO]
    with HttpModule[IO]
    with ConfigurationModule
    with ClusterModule[IO] =>

  val clock: Clock = Clock.systemUTC()

  val authService: AuthServiceImpl[IO] = new AuthServiceImpl[IO](accountService)
  val accountController = new AccountController(authService, accountService)
  val clusterController = new ClusterController(clusterService)
  val workspaceController =
    new WorkspaceController(
      authService,
      workspaceService,
      memberService,
      provisionService,
      clock
    )
  val templateController = new TemplateController(authService)

  val restAPI: RestAPI = new RestAPI(
    accountController,
    clusterController,
    workspaceController,
    templateController
  )
}
