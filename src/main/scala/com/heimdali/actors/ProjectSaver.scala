package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.ExecutionContext
import akka.pattern.pipe

object ProjectSaver {

  sealed trait Update { def project: Project }

  final case class LDAPUpdate(project: Project) extends Update
  final case class HDFSUpdate(project: Project) extends Update

  case object ProjectSaved

}

class ProjectSaver @Inject() (projectRepository: ProjectRepository)
                             (implicit val executionContext: ExecutionContext)
  extends Actor {

  import ProjectSaver._

  override def receive: Receive = {
    case update: LDAPUpdate =>
      val project = update.project
      projectRepository.setLDAP(project.id, project.systemName, project.ldapDn.get)
        .map(_ => ProjectSaved)
        .pipeTo(sender())

    case update: HDFSUpdate =>
      val project = update.project
      projectRepository.setHDFS(project.id, project.hdfs.location.get)
        .map(_ => ProjectSaved)
        .pipeTo(sender())
  }

}
