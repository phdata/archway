package com.heimdali.modules

import cats.effect.IO
import com.heimdali.rest._

trait RestModule {
  this: ServiceModule[IO]
    with HttpModule[IO]
    with ConfigurationModule
    with ClusterModule[IO] =>

  val authService: AuthServiceImpl[IO] = new AuthServiceImpl[IO](accountService)
  val accountController = new AccountController(authService, accountService)
  val clusterController = new ClusterController(clusterService)
  val workspaceController = new WorkspaceController(authService, workspaceService)
  val templateController = new TemplateController(authService)

  val restAPI: RestAPI = new RestAPI(accountController, clusterController, workspaceController, templateController)
}
