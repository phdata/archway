package com.heimdali.modules

import akka.actor.ActorRef
import com.heimdali.models.{Dataset, SharedWorkspace, UserWorkspace}
import com.heimdali.provisioning.WorkspaceProvisioner
import com.heimdali.services._

trait ServiceModule {
  this: AkkaModule
    with ExecutionContextModule
    with RepoModule
    with ClientModule
    with RepoModule
    with ConfigurationModule
    with HttpModule =>

  val userProvisionerFactory: UserWorkspace => ActorRef =
    (userWorkspace) => actorSystem.actorOf(WorkspaceProvisioner.props[String, UserWorkspace](ldapActor, hdfsActor, hiveActor, userWorkspaceSaver, configuration, userWorkspace))

  val accountService: AccountService = new AccountServiceImpl(ldapClient, accountRepository, configuration, userProvisionerFactory)

  val clusterService: ClusterService = new CDHClusterService(http, configuration)

  val workspaceProvisionerFactory: SharedWorkspace => ActorRef =
    (sharedWorkspace) => actorSystem.actorOf(WorkspaceProvisioner.props[Long, SharedWorkspace](ldapActor, hdfsActor, hiveActor, sharedWorkspaceSaver, configuration, sharedWorkspace))

  val workspaceService: WorkspaceService = new WorkspaceServiceImpl(ldapClient, sharedWorkspaceRepository, complianceRepository, workspaceProvisionerFactory)

  val keytabService: KeytabService = new KeytabServiceImpl()

  val hiveService: HiveService = new HiveServiceImpl

  val datasetProvisionerFactory: Dataset => ActorRef =
    (dataset) => actorSystem.actorOf(WorkspaceProvisioner.props[Long, Dataset](ldapActor, hdfsActor, hiveActor, datasetWorkspaceSaver, configuration, dataset))

  val environment = configuration.getString("cluster.environment")

  val governedDatasetService: GovernedDatasetService = new GovernedDatasetServiceImpl(governedDatasetRepository, datasetRepository, complianceRepository, environment, ldapClient, datasetProvisionerFactory)
}
