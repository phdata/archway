package com.heimdali.provisioning

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.heimdali.clients.LDAPClient

import scala.concurrent.{ExecutionContext, Future}

object LDAPActor {

  case class CreateGroup(name: String, initialMembers: Seq[String])

  case class LDAPGroupCreated(groupName: String, dn: String)

  def props(lDAPClient: LDAPClient)(implicit executionContext: ExecutionContext) =
    Props(classOf[LDAPActor], lDAPClient, executionContext)

}

class LDAPActor(ldapClient: LDAPClient)
               (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import LDAPActor._

  override def receive: Receive = {
    case CreateGroup(name, users) =>
      log.info("creating group {}", name)
      (for {
        group <- ldapClient.createGroup(name)
        _ <- Future.traverse(users)(ldapClient.addUser(name, _))
      } yield LDAPGroupCreated(name, group))
        .pipeTo(sender())
  }

}
