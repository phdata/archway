package com.heimdali.modules

import java.sql.DriverManager

import akka.actor.{ActorRef, Props}
import com.heimdali.models.{Dataset, GovernedDataset, SharedWorkspace, UserWorkspace}
import com.heimdali.provisioning.{WorkspaceProvisioner, _}


trait AkkaModule {
  this: ExecutionContextModule
    with ConfigurationModule
    with ServiceModule
    with ClientModule
    with RepoModule
    with ContextModule =>

  val hiveConfig = configuration.getConfig("db.hive")
  Class.forName(hiveConfig.getString("driver"))
  val hiveConnectionFactory =
    () => DriverManager.getConnection(hiveConfig.getString("url"), "", "")

  val ldapActor: ActorRef = actorSystem.actorOf(LDAPActor.props(ldapClient))
  val sharedWorkspaceSaver: ActorRef = actorSystem.actorOf(WorkspaceSaver[Long, SharedWorkspace](sharedWorkspaceRepository, ldapRepository, hiveDatabaseRepository))
  val userWorkspaceSaver: ActorRef = actorSystem.actorOf(WorkspaceSaver[String, UserWorkspace](userWorkspaceRepository, ldapRepository, hiveDatabaseRepository))
  val datasetWorkspaceSaver: ActorRef = actorSystem.actorOf(WorkspaceSaver[Long, Dataset](datasetRepository, ldapRepository, hiveDatabaseRepository))
  val hdfsActor: ActorRef = actorSystem.actorOf(HDFSActor.props(hdfsClient, loginContextProvider))
  val keytabActor: ActorRef = actorSystem.actorOf(KeytabActor.props(hdfsClient, keytabService, configuration))
  val yarnActor: ActorRef = actorSystem.actorOf(YarnActor.props(yarnClient))
  val hiveActor: ActorRef = actorSystem.actorOf(HiveActor.props(configuration, hadoopConfiguration, hiveService, hiveConnectionFactory))

}