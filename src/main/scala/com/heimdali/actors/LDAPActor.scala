package com.heimdali.actors

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.heimdali.actors.WorkspaceSaver.ProjectUpdate
import com.heimdali.models.ViewModel._
import com.heimdali.services.LDAPClient

import scala.concurrent.ExecutionContext

object LDAPActor {

  case class CreateSharedWorkspaceGroup(name: String, initialMembers: Seq[String])

  case class CreateUserWorkspaceGroup(username: String)

  case class LDAPGroupCreated(groupName: String, dn: String)

}

class LDAPActor(ldapClient: LDAPClient)
               (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import LDAPActor._

  override def receive: Receive = {
    case CreateSharedWorkspaceGroup(name, users) =>
      createGroup(s"edh_sw_$name", users)

    case CreateUserWorkspaceGroup(username) =>
      createGroup(s"edh_user_$username", Seq(username))
  }

  def createGroup(groupName: String, users: Seq[String]): Unit = {
    log.info("creating group {}", groupName)
    ldapClient
      .createGroup(groupName, users.head)
      .map(LDAPGroupCreated(groupName, _))
      .pipeTo(sender())
  }

}
