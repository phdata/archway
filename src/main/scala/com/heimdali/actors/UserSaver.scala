package com.heimdali.actors

import akka.actor.Actor
import com.heimdali.repositories.AccountRepository
import com.heimdali.services.UserWorkspace

import akka.pattern.pipe
import scala.concurrent.ExecutionContext

object UserSaver {

  case class SaveUser(userWorkspace: UserWorkspace)

  case object UserSaved

}

class UserSaver(accountRepository: AccountRepository)
               (implicit executionContext: ExecutionContext)
  extends Actor {

  import UserSaver._

  override def receive: Receive = {
    case SaveUser(workspace) =>
      accountRepository
        .create(workspace)
        .map(_ => UserSaved)
        .pipeTo(sender())
  }
}
