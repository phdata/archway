package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.ExecutionContext

object ProjectSaver {

  final case class UpdateProject(project: Project)
  case object ProjectSaved

}

class ProjectSaver @Inject() (projectRepository: ProjectRepository)
                             (implicit val executionContext: ExecutionContext)
  extends Actor {

  import ProjectSaver._

  override def receive: Receive = {
    case up @ UpdateProject(project) =>
      val respondTo = sender()
      projectRepository.setLDAP(project.id, project.systemName, project.ldapDn.get) map { _ =>
        respondTo ! ProjectSaved
      }
  }

}
