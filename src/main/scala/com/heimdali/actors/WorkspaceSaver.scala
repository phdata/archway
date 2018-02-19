package com.heimdali.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.KeytabActor.KeytabCreated
import com.heimdali.models.ViewModel._
import com.heimdali.repositories.WorkspaceRepository

import scala.concurrent.ExecutionContext

object WorkspaceSaver {

  case class LDAPUpdate(id: Long, dn: String)

  case class HDFSUpdate(id: Long, location: String, size: Double)

  trait ProjectUpdate {
    def updateProject(project: SharedWorkspace): SharedWorkspace
  }

  case object WorkspaceSaved

}

class WorkspaceSaver(projectRepository: WorkspaceRepository)
                    (implicit val executionContext: ExecutionContext)
  extends Actor {

  import WorkspaceSaver._

  override def receive: Receive = {
    case LDAPUpdate(id, dn) =>
      projectRepository.setLDAP(id, dn)
        .map(_ => WorkspaceSaved)
        .pipeTo(sender())

    case HDFSUpdate(id, location, actualGB) =>
      projectRepository.setHDFS(id, location, actualGB)
        .map(_ => WorkspaceSaved)
        .pipeTo(sender())

    case KeytabCreated(id, location) =>
      projectRepository.setKeytab(id, location.toUri.toString)
        .map(_ => WorkspaceSaved)
        .pipeTo(sender())
  }

}
