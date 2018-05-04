package com.heimdali.provisioning

import akka.actor.{ActorLogging, ActorRef, FSM, Props}
import com.heimdali.models.{LDAPRegistration, Workspace}
import com.heimdali.provisioning.HiveActor.{CreateDatabase, DatabaseCreated}
import com.typesafe.config.Config


object WorkspaceProvisioner {

  case object Start

  case object Started

  def props[T <: Workspace](ldapActor: ActorRef, hdfsActor: ActorRef, hiveActor: ActorRef, saveActor: ActorRef, configuration: Config, sharedWorkspace: T) =
    Props(new WorkspaceProvisioner[T](ldapActor, hdfsActor, hiveActor, saveActor, configuration, sharedWorkspace))

}

class WorkspaceProvisioner[T <: Workspace](ldapActor: ActorRef,
                                           hdfsActor: ActorRef,
                                           hiveActor: ActorRef,
                                           saveActor: ActorRef,
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
      ldapActor ! CreateGroup(workspace.groupName, workspace.initialMembers)
      hdfsActor ! CreateDirectory(workspace.dataDirectory(configuration), 0, workspace.onBehalfOf)
      goto(Provisioning) replying Started
  }

  when(Provisioning) {
    case Event(DirectoryCreated(_), _) =>
      hiveActor ! CreateDatabase(workspace.groupName, workspace.databaseName, workspace.role, workspace.dataDirectory(configuration))
      stay()

    case Event(DatabaseCreated(db), _) =>
      saveActor ! HiveUpdate(workspace.workspaceId, db)
      stay()

    case Event(LDAPGroupCreated(name, dn), _) =>
      saveActor ! LDAPUpdate(workspace.workspaceId, LDAPRegistration(None, dn, name))
      stay()
  }

  whenUnhandled {
    case Event(e, s) ⇒
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay()
  }

  //TODO: Clean up
}