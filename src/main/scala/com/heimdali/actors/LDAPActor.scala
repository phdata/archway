package com.heimdali.actors

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.heimdali.actors.WorkspaceSaver.ProjectUpdate
import com.heimdali.models.ViewModel._
import com.heimdali.services.LDAPClient

import scala.concurrent.ExecutionContext

object LDAPActor {

  case class CreateEntry(id: Long, name: String, initialMembers: Seq[String])

  case class LDAPUpdate(id: Long, projectDn: String) extends ProjectUpdate {
    def updateProject(project: SharedWorkspace): SharedWorkspace =
      project.copy(ldapDn = Some(projectDn))
  }

}

class LDAPActor(ldapClient: LDAPClient)
               (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import LDAPActor._

  override def receive: Receive = {
    case CreateEntry(id, name, users) =>
      log.info("creating group {}", name)
      ldapClient
        .createGroup(name, users.head)
        .map(LDAPUpdate(id, _))
        .pipeTo(sender())
  }

}
