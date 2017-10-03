package com.heimdali.actors

import javax.inject.{Inject, Named}

import akka.actor.{ActorLogging, ActorRef, FSM}
import com.heimdali.actors
import com.heimdali.models.{HDFSProvision, Project}
import com.heimdali.services.HDFSAllocation

import scala.collection.mutable.ListBuffer

sealed trait State

sealed trait Data

sealed trait Step

object ProjectProvisioner {

  case object CreateLDAPEntry extends Step

  case object CreateHDFSAllocations extends Step

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
                                   @Named("project-saver") saveActor: ActorRef,
                                   @Named("hdfs-actor") hDFSActor: ActorRef)
  extends FSM[State, Data] with ActorLogging {

  import LDAPActor._
  import ProjectProvisioner._
  import ProjectSaver._
  import HDFSActor._

  val initialSteps: ListBuffer[Step] = ListBuffer(
    CreateLDAPEntry,
    CreateHDFSAllocations
  )

  startWith(Ready, NotProvisioned(ActorRef.noSender))

  when(Ready) {
    case Event(RegisterCaller(ref), _) =>
      stay() using NotProvisioned(ref)

    case Event(Request(project), NotProvisioned(ref)) =>
      log.info("received a request to provision {}", project)
      val create = CreateEntry(project.systemName, Seq(project.createdBy))
      log.info("sending {} to {}", create, ldapActor)
      ldapActor ! create
      goto(Provisioning) using Provision(ref, initialSteps, project)
  }

  when(Provisioning) {
    case Event(LDAPDone(dn), Provision(ref, remaining, project)) =>
      log.info("done creating ldap group {}", dn)
      val updatedProject = project.copy(ldapDn = Some(dn))
      saveActor ! LDAPUpdate(updatedProject)
      goto(Saving) using Provision(ref, remaining - CreateLDAPEntry, updatedProject)
    case Event(HDFSDone(location), Provision(ref, remaining, project)) =>
      log.info("done creating HDFS directory {}", location)
      val updatedProject = project.copy(hdfs = HDFSProvision(Some(location), project.hdfs.requestedSizeInGB))
      saveActor ! HDFSUpdate(updatedProject)
      goto(Saving) using Provision(ref, remaining - CreateHDFSAllocations, updatedProject)
  }

  when(Saving) {
    case Event(ProjectSaved, provision@Provision(ref, remaining, project)) =>
      remaining.toSeq match {
        case List() =>
          log.info("everything is saved for {}", project)
          goto(Completed) using Save(ref, project)
        case CreateHDFSAllocations :: _ =>
          log.info("creating HDFS provisions for {}", project)
          hDFSActor ! CreateDirectory(project.systemName, project.hdfs.requestedSizeInGB)
          goto(Provisioning) using provision
      }
  }

  when(Completed) {
    case _ => stay()
  }

  onTransition {

    case Saving -> Completed =>
      stateData match {
        case Provision(ref, _, _) if ref != null =>
          ref ! ProvisionCompleted
        case _ =>
      }

  }

  initialize()

}
