package com.heimdali.modules

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import cats.effect.IO
import com.heimdali.actors._
import com.heimdali.actors.user.UserProvisioner
import com.heimdali.actors.workspace.WorkspaceProvisioner
import com.heimdali.models.ViewModel.SharedWorkspace
import com.heimdali.services.{LoginContextProvider, User, UserWorkspace}
import doobie.util.transactor.Transactor


trait AkkaModule {
  this: ExecutionContextModule
    with ConfigurationModule
    with ServiceModule
    with ClientModule
    with RepoModule
    with ContextModule =>

  val hiveConfig = configuration.getConfig("db.hive")

  val hiveTransactor = Transactor.fromDriverManager[IO](
    metaConfig.getString("driver"),
    metaConfig.getString("url")
  )

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  val ldapActor: ActorRef = actorSystem.actorOf(Props(classOf[LDAPActor], ldapClient, executionContext))
  val saveActor: ActorRef = actorSystem.actorOf(Props(classOf[WorkspaceSaver], workspaceRepository, executionContext))
  val hDFSActor: ActorRef = actorSystem.actorOf(Props(classOf[HDFSActor], hdfsClient, loginContextProvider, configuration, executionContext))
  val keytabActor: ActorRef = actorSystem.actorOf(Props(classOf[KeytabActor], hdfsClient, keytabService, configuration, executionContext))
  val yarnActor: ActorRef = actorSystem.actorOf(Props(classOf[YarnActor], yarnClient, executionContext))
  val hiveActor: ActorRef = actorSystem.actorOf(HiveActor.props(configuration, hadoopConfiguration, hiveService))
  val userSaveActor: ActorRef = actorSystem.actorOf(Props(classOf[UserSaver], accountRepository, executionContext))

  val userProvisionerFactory: User => ActorRef =
    (user) => actorSystem.actorOf(UserProvisioner.props(hiveActor, ldapActor, userSaveActor, hDFSActor, UserWorkspace(user.username, "", "", "")))

  val workspaceProvisionerFactory: SharedWorkspace => ActorRef =
    (sharedWorkspace: SharedWorkspace) =>
      actorSystem.actorOf(Props(classOf[WorkspaceProvisioner], ldapActor, saveActor, hDFSActor, keytabActor, yarnActor, sharedWorkspace))

}