package com.heimdali.actors

import javax.inject.Inject

import akka.actor.Actor
import com.heimdali.services.LDAPClient

import scala.concurrent.ExecutionContext

object LDAPActor {

  case class CreateEntry(name: String, initialMembers: Seq[String])

  case class LDAPDone(projectDn: String)

}

class LDAPActor @Inject()(ldapClient: LDAPClient)
                         (implicit val executionContext: ExecutionContext) extends Actor {

  import LDAPActor._

  override def receive: Receive = {
    case request@CreateEntry(name, users) =>
      val replyTo = sender()
      ldapClient.createGroup(name, users.head) map { dn =>
        replyTo ! LDAPDone(dn)
      }
  }

}
