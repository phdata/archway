package com.heimdali.actors

import akka.actor.{Actor, ActorLogging, ActorRef, FSM}
import com.heimdali.models.ViewModel._

import scala.collection.immutable.Queue

sealed trait State

sealed trait Data

sealed trait Step

object WorkspaceProvisioner {

  case object Request

  case object ProvisionCompleted

  final case class RegisterCaller(ref: ActorRef)

  case object Ready extends State

  case object Provisioning extends State

  case object Saving extends State

  case object Completed extends State

  final case class NotProvisioned(ref: ActorRef, queue: Queue[(ActorRef, Any)]) extends Data

  final case class Provision(ref: ActorRef, queue: Queue[(ActorRef, Any)]) extends Data

  final case class Saved(ref: ActorRef) extends Data

  trait Factory {
    def apply(project: SharedWorkspace): Actor
  }

}

class WorkspaceProvisioner(ldapActor: ActorRef,
                           saveActor: ActorRef,
                           hDFSActor: ActorRef,
                           keytabActor: ActorRef,
                           yarnActor: ActorRef,
                           var project: SharedWorkspace)
  extends FSM[State, Data] with ActorLogging {

  log.info("starting up")

  import HDFSActor._
  import YarnActor._
  import KeytabActor._
  import LDAPActor._
  import WorkspaceProvisioner._
  import WorkspaceSaver._

  val initialSteps: Queue[(ActorRef, AnyRef)] = Queue(
    ldapActor -> CreateEntry(project.id, project.systemName, Seq(project.createdBy)),
    hDFSActor -> CreateDirectory(project.id, project.systemName, project.hdfs.requestedSizeInGB)
//    keytabActor -> GenerateKeytab(project.id, project.systemName),
//    yarnActor -> CreatePool(project.id, project.systemName, project.yarn.maxCores, project.yarn.maxMemoryInGB)
  )

  def dequeue(queue: Queue[(ActorRef, Any)]): Queue[(ActorRef, Any)] = {
    val ((actor, message), newQueue) = queue.dequeue
    log.info("sending {} to {}", message, actor)
    actor ! message
    newQueue
  }

  startWith(Ready, NotProvisioned(ActorRef.noSender, initialSteps))

  when(Ready) {
    case Event(RegisterCaller(ref), NotProvisioned(_, queue)) =>
      stay() using NotProvisioned(ref, queue)

    case Event(Request, NotProvisioned(ref, queue)) =>
      log.info("received a request to provision {}", project)
      goto(Provisioning) using Provision(ref, dequeue(queue))
  }

  when(Provisioning) {
    case Event(projectUpdate: ProjectUpdate, existingState) =>
      log.info("saving project with {}", projectUpdate)
      saveActor ! projectUpdate
      project = projectUpdate.updateProject(project)
      goto(Saving) using existingState
  }

  when(Saving) {
    case Event(WorkspaceSaved, Provision(ref, queue)) =>
      if (queue.isEmpty) {
        log.info("everything is saved for {}", project)
        goto(Completed) using Saved(ref)
      } else {
        goto(Provisioning) using Provision(ref, dequeue(queue))
      }
  }

  when(Completed) {
    case _ => stay()
  }

  onTransition {

    case Saving -> Completed =>
      stateData match {
        case Provision(ref, _) if ref != null =>
          ref ! ProvisionCompleted
        case _ =>
      }

  }

  initialize()

}
