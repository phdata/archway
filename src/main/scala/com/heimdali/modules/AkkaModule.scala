package com.heimdali.modules

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, Materializer}
import com.heimdali.actors._
import com.heimdali.models.ViewModel.SharedWorkspace


trait AkkaModule {
  this: ExecutionContextModule with ConfigurationModule with ServiceModule with ClientModule with RepoModule =>

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  val ldapActor: ActorRef = actorSystem.actorOf(Props(classOf[LDAPActor], ldapClient, executionContext))
  val saveActor: ActorRef = actorSystem.actorOf(Props(classOf[WorkspaceSaver], workspaceRepository, executionContext))
  val hDFSActor: ActorRef = actorSystem.actorOf(Props(classOf[HDFSActor], hdfsClient, configuration, executionContext))
  val keytabActor: ActorRef = actorSystem.actorOf(Props(classOf[KeytabActor], hdfsClient, keytabService, configuration, executionContext))
  val yarnActor: ActorRef = actorSystem.actorOf(Props(classOf[YarnActor], yarnClient, executionContext))

  val workspaceProvisionerFactory: SharedWorkspace => ActorRef =
    (sharedWorkspace: SharedWorkspace) =>
      actorSystem.actorOf(Props(classOf[WorkspaceProvisioner], ldapActor, saveActor, hDFSActor, keytabActor, yarnActor, sharedWorkspace))

}