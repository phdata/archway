package com.heimdali.modules

import java.sql.{Connection, DriverManager}

import akka.actor.ActorRef
import com.heimdali.models.{Dataset, SharedWorkspace, UserWorkspace}
import com.heimdali.provisioning._
import com.typesafe.config.Config


trait AkkaModule {
  this: ExecutionContextModule
    with ConfigurationModule
    with ServiceModule
    with ClientModule
    with RepoModule
    with ContextModule =>

  val hiveConfig: Config = configuration.getConfig("db.hive")

  Class.forName(hiveConfig.getString("driver"))

  val hiveConnectionFactory: () => Connection = () => DriverManager.getConnection(hiveConfig.getString("url"), "", "")

  val ldapActor: ActorRef = actorSystem.actorOf(LDAPActor.props(ldapClient))
  val sharedWorkspaceSaver: ActorRef = actorSystem.actorOf(WorkspaceSaver[Long, SharedWorkspace](sharedWorkspaceRepository, ldapRepository, hiveDatabaseRepository, yarnRepository))
  val userWorkspaceSaver: ActorRef = actorSystem.actorOf(WorkspaceSaver[String, UserWorkspace](userWorkspaceRepository, ldapRepository, hiveDatabaseRepository, yarnRepository))
  val datasetWorkspaceSaver: ActorRef = actorSystem.actorOf(WorkspaceSaver[Long, Dataset](datasetRepository, ldapRepository, hiveDatabaseRepository, yarnRepository))
  val hdfsActor: ActorRef = actorSystem.actorOf(HDFSActor.props(hdfsClient, loginContextProvider))
  val keytabActor: ActorRef = actorSystem.actorOf(KeytabActor.props(hdfsClient, keytabService, configuration))
  val yarnActor: ActorRef = actorSystem.actorOf(YarnActor.props(yarnClient))
  val hiveActor: ActorRef = actorSystem.actorOf(HiveActor.props(configuration, hadoopConfiguration, hiveService, hiveConnectionFactory))

}