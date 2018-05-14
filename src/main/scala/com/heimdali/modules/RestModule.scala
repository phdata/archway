package com.heimdali.modules

import com.heimdali.rest._

trait RestModule {
  this: AkkaModule
    with ExecutionContextModule
    with ServiceModule
    with HttpModule
    with ConfigurationModule
    with ClusterModule =>

  val authService: AuthServiceImpl = new AuthServiceImpl(accountService)
  val accountController = new AccountController(authService, accountService, configuration)
  val clusterController = new ClusterController(clusterService)
  val workspaceController = new WorkspaceController(authService, workspaceService)
  val governedDatasetController = new GovernedDatasetController(authService, governedDatasetService, configuration)

  val restAPI = new RestAPI(httpExt, configuration, accountController, clusterController, workspaceController, governedDatasetController)
}
