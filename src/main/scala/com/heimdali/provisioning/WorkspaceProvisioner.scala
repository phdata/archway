package com.heimdali.provisioning

import akka.actor.{ActorLogging, ActorRef, FSM, Props}
import com.heimdali.models.{LDAPRegistration, Workspace, Yarn}
import com.heimdali.provisioning.HiveActor.{CreateDatabase, DatabaseCreated}
import com.heimdali.provisioning.YarnActor.{CreatePool, PoolCreated}
import com.typesafe.config.Config

import scala.util.Failure


object WorkspaceProvisioner {

  case object Start

  case object Started

  def props[A, T <: Workspace[A]](ldapActor: ActorRef, hdfsActor: ActorRef, hiveActor: ActorRef, saveActor: ActorRef, yarnActor: ActorRef, configuration: Config, sharedWorkspace: T) =
    Props(new WorkspaceProvisioner[A, T](ldapActor, hdfsActor, hiveActor, saveActor, yarnActor, configuration, sharedWorkspace))

}

class WorkspaceProvisioner[A, T <: Workspace[A]](ldapActor: ActorRef,
                                                 hdfsActor: ActorRef,
                                                 hiveActor: ActorRef,
                                                 saveActor: ActorRef,
                                                 yarnActor: ActorRef,
                                                 configuration: Config,
                                                 workspace: T)
  extends FSM[WorkspaceState, T] with ActorLogging {

  import HDFSActor._
  import LDAPActor._
  import WorkspaceProvisioner._
  import WorkspaceSaver._

  startWith(Idle, workspace)

  when(Idle) {
    case Event(Start, _) =>
      ldapActor ! CreateGroup(workspace.groupName(configuration), workspace.initialMembers)
      hdfsActor ! CreateDirectory(workspace.dataDirectory(configuration), workspace.requestedDiskSize(configuration), workspace.onBehalfOf)
      yarnActor ! CreatePool(workspace.parentPools(configuration), workspace.poolName, workspace.requestedCores(configuration), workspace.requestedMemory(configuration))
      goto(Provisioning) replying Started
  }

  when(Provisioning) {
    case Event(DirectoryCreated(_), _) =>
      hiveActor ! CreateDatabase(workspace.groupName(configuration), workspace.databaseName, workspace.role(configuration), workspace.dataDirectory(configuration))
      stay()

    case Event(DatabaseCreated(db), _) =>
      saveActor ! HiveUpdate[A](workspace.workspaceId, db.copy(sizeInGB = workspace.requestedDiskSize(configuration)))
      stay()

    case Event(LDAPGroupCreated(name, dn), _) =>
      saveActor ! LDAPUpdate[A](workspace.workspaceId, LDAPRegistration(None, dn, name))
      stay()

    case Event(PoolCreated(name), _) =>
      saveActor ! YarnUpdate[A](workspace.workspaceId, Yarn(None, name, workspace.requestedCores(configuration), workspace.requestedMemory(configuration)))
      stay()
  }

  whenUnhandled {
    case Event(Failure(exc), _) =>
      log.error(exc, "Failure during provisioning: {}", exc.getMessage)
      stop()
    case Event(e, s) ⇒
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay()
  }

  //TODO: Clean up
}