package com.heimdali.actors.user

import akka.actor.{ActorRef, Props}
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import com.heimdali.actors.HDFSActor.CreateUserDirectory
import com.heimdali.actors.HiveActor.{CreateUserDatabase, DatabaseCreated}
import com.heimdali.actors.LDAPActor.{CreateUserWorkspaceGroup, LDAPGroupCreated}
import com.heimdali.actors.UserSaver.{SaveUser, UserSaved}
import com.heimdali.actors._
import com.heimdali.services.UserWorkspace

import scala.reflect.ClassTag

sealed trait UserState extends FSMState

case object Idle extends UserState {
  override def identifier = "Waiting to provision"
}

case object Provisioning extends UserState {
  override def identifier = "Provisioning user workspace"
}

case object Saving extends UserState {
  override def identifier = "Saving user workspace"
}

sealed trait UserEvent

case object Start extends UserEvent

case object CreateGroup extends UserEvent

case object CreateDB extends UserEvent

case class Save(database: HiveDatabase) extends UserEvent

object UserProvisioner {

  case object Request

  case object Started

  def props(hiveActor: ActorRef, ldapActor: ActorRef, saveActor: ActorRef, hdfsActor: ActorRef, userWorkspace: UserWorkspace) =
    Props(new UserProvisioner(hiveActor, ldapActor, saveActor, hdfsActor, userWorkspace))

}

class UserProvisioner(hiveActor: ActorRef,
                      ldapActor: ActorRef,
                      saveActor: ActorRef,
                      hdfsActor: ActorRef,
                      workspace: UserWorkspace)
                     (implicit val domainEventClassTag: ClassTag[UserEvent])
  extends PersistentFSM[UserState, UserWorkspace, UserEvent] {

  import UserProvisioner._

  startWith(Idle, workspace)

  when(Idle) {
    case Event(Request, _) =>
      goto(Provisioning) applying Start replying Started andThen { workspace =>
        ldapActor ! CreateUserWorkspaceGroup(workspace.username)
        hdfsActor ! CreateUserDirectory(workspace.username)
      }
  }

  when(Provisioning) {
    case Event(LDAPGroupCreated(_, _), _) =>
      stay() applying CreateDB andThen { existing =>
        hiveActor ! CreateUserDatabase(existing.username)
      }

    case Event(DatabaseCreated(db), existing) =>
      goto(Saving) applying Save(db) andThen { newWorkspace =>
        saveActor ! SaveUser(newWorkspace)
      }
  }

  when(Saving) {
    case Event(UserSaved, _) =>
      stop()
  }

  override def applyEvent(domainEvent: UserEvent, currentData: UserWorkspace): UserWorkspace =
    domainEvent match {
      case Save(db) =>
        currentData.copy(database = db)
      case _ =>
        currentData
    }

  override def persistenceId: String = workspace.username

}