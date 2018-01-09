package com.heimdali.modules

import com.heimdali.services._

trait ServiceModule {
  this: AkkaModule
    with ExecutionContextModule
    with RepoModule
    with ClientModule
    with ConfigurationModule
    with HttpModule =>

  val accountService: AccountService = new LDAPAccountService(ldapClient, configuration)

  val clusterService: ClusterService = new CDHClusterService(http, configuration)

  val workspaceService: WorkspaceService = new WorkspaceServiceImpl(workspaceRepository, workspaceProvisionerFactory)

  val keytabService: KeytabService = new KeytabServiceImpl()
}
