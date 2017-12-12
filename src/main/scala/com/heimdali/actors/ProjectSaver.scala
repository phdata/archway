package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import com.heimdali.Model._
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.ExecutionContext
import akka.pattern.pipe
import com.heimdali.actors.HDFSActor.HDFSUpdate
import com.heimdali.actors.KeytabActor.KeytabCreated
import com.heimdali.actors.LDAPActor.LDAPUpdate

object ProjectSaver {

  trait ProjectUpdate { def updateProject(project: Project): Project }

  case object ProjectSaved

}

class ProjectSaver @Inject() (projectRepository: ProjectRepository)
                             (implicit val executionContext: ExecutionContext)
  extends Actor {

  import ProjectSaver._

  override def receive: Receive = {
    case LDAPUpdate(id, dn) =>
      projectRepository.setLDAP(id, dn)
        .map(_ => ProjectSaved)
        .pipeTo(sender())

    case HDFSUpdate(id, location) =>
      projectRepository.setHDFS(id, location)
        .map(_ => ProjectSaved)
        .pipeTo(sender())

    case KeytabCreated(id, location) =>
      projectRepository.setKeytab(id, location.toUri.toString)
        .map(_ => ProjectSaved)
        .pipeTo(sender())
  }

}
