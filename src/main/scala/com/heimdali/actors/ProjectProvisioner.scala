package com.heimdali.actors

import javax.inject.{Inject, Named}

import akka.actor.{Actor, ActorLogging, ActorRef, FSM}
import com.heimdali.actors
import com.heimdali.models.Project

import scala.collection.mutable.ListBuffer

sealed trait State

sealed trait Data

sealed trait Step

object ProjectProvisioner {

  case object CreateLDAPEntry extends Step

  final case class Request(project: Project)
  case object ProvisionCompleted
  final case class RegisterCaller(ref: ActorRef)

  case object Ready extends State
  case object Provisioning extends State
  case object Saving extends State
  case object Completed extends State

  case class NotProvisioned(ref: ActorRef) extends Data
  case class Provision(ref: ActorRef, remaining: ListBuffer[Step], project: Project) extends Data
  case class Save(ref: ActorRef, project: Project) extends Data

}

class ProjectProvisioner @Inject()(@Named("ldap-actor") ldapActor: ActorRef,
                                   @Named("project-saver") saveActor: ActorRef)
  extends FSM[State, Data] with ActorLogging {

  import LDAPActor._
  import ProjectProvisioner._
  import ProjectSaver._

  startWith(Ready, NotProvisioned(ActorRef.noSender))

  when(Ready) {
    case Event(RegisterCaller(ref), _) =>
      stay() using NotProvisioned(ref)

    case Event(Request(project), NotProvisioned(ref)) =>
      log.info("received a request to provision {}", project)
      val create = CreateEntry(project.systemName, Seq(project.createdBy))
      ldapActor ! create
      goto(Provisioning) using Provision(ref, ListBuffer(CreateLDAPEntry), project)
  }

  when(Provisioning) {
    case Event(LDAPDone(dn), Provision(ref, remaining, project)) =>
      next(CreateLDAPEntry, ref, remaining, project.copy(ldapDn = Some(dn)))
  }

  when(Saving) {
    case Event(ProjectSaved, stateData) =>
      goto(Completed) using stateData
  }

  when(Completed) {
    case _ => stay()
  }

  onTransition {

    case Saving -> Completed =>
      stateData match {
        case Save(ref, _) if ref != null =>
          ref ! ProvisionCompleted
        case _ =>
      }

  }

  initialize()

  def next(thisStep: Step, ref: ActorRef, remaining: ListBuffer[Step], project: Project): FSM.State[actors.State, Data] = {
    val newRemaining = remaining - thisStep
    if (newRemaining.isEmpty) {
      saveActor ! UpdateProject(project)
      goto(Saving) using Save(ref, project)
    } else
      stay() using Provision(ref, newRemaining, project)
  }

}