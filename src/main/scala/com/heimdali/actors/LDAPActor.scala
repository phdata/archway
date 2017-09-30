package com.heimdali.actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import com.heimdali.services.LDAPClient

import scala.concurrent.ExecutionContext

object LDAPActor {

  case class CreateEntry(name: String, initialMembers: Seq[String])

  case class LDAPDone(projectDn: String)

}

class LDAPActor @Inject()(ldapClient: LDAPClient)
                         (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import LDAPActor._

  override def receive: Receive = {
    case request@CreateEntry(name, users) =>
      log.info("creating group {}", name)
      val replyTo = sender()
      ldapClient.createGroup(name, users.head) map { dn =>
        log.info("created LDAP group {}", name)
        replyTo ! LDAPDone(dn)
      }
  }

}
