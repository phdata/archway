package com.heimdali.actors.workspace

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import com.heimdali.actors._
import com.heimdali.models.ViewModel._

import scala.reflect.ClassTag

sealed trait WorkspaceState extends FSMState

case object Idle extends WorkspaceState {
  override def identifier = "Waiting to provision"
}

case object Provisioning extends WorkspaceState {
  override def identifier = "Provisioning shared workspace"
}

case object Saving extends WorkspaceState {
  override def identifier = "Saving shared workspace"
}

sealed trait WorkspaceEvent

case object Start extends WorkspaceEvent

object WorkspaceProvisioner {

  case object Request

  case object Started

  def props(hiveActor: ActorRef, ldapActor: ActorRef, saveActor: ActorRef, keytabActor: ActorRef, yarnActor: ActorRef, hdfsActor: ActorRef, project: SharedWorkspace) =
    Props(new WorkspaceProvisioner(ldapActor, saveActor, hdfsActor, keytabActor, yarnActor, project))

}

class WorkspaceProvisioner(ldapActor: ActorRef,
                           saveActor: ActorRef,
                           hDFSActor: ActorRef,
                           keytabActor: ActorRef,
                           yarnActor: ActorRef,
                           var project: SharedWorkspace)
                          (implicit val domainEventClassTag: ClassTag[WorkspaceEvent])
  extends PersistentFSM[WorkspaceState, SharedWorkspace, WorkspaceEvent] with ActorLogging {

  import HDFSActor._
  import LDAPActor._
  import WorkspaceProvisioner._

  startWith(Idle, project)

  when(Idle) {
    case Event(Request, _) =>
      goto(Provisioning) applying Start replying Started andThen { workspace =>
        ldapActor ! CreateSharedWorkspaceGroup(workspace.systemName, Seq(workspace.createdBy))
        hDFSActor ! CreateSharedDirectory(workspace.systemName, 0)
      }
  }

  override def applyEvent(domainEvent: WorkspaceEvent, currentData: SharedWorkspace): SharedWorkspace =
    domainEvent match {
      case _ =>
        currentData
    }

  override def persistenceId: String = project.id.toString
}
