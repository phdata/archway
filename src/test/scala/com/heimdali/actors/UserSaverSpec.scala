package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.actors.UserSaver.{SaveUser, UserSaved}
import com.heimdali.repositories.AccountRepository
import com.heimdali.services.UserWorkspace
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class UserSaverSpec extends FlatSpec with MockFactory {

  behavior of "User Saver"

  it should "save a user" in new TestKit(ActorSystem()) with ImplicitSender {
    val userWorkspace = UserWorkspace("username", "database", "/data", "role")
    val workspaceRepository = mock[AccountRepository]
    (workspaceRepository.create _).expects(userWorkspace).returning(Future(userWorkspace))

    val actor = system.actorOf(Props(classOf[UserSaver], workspaceRepository, ExecutionContext.global))

    actor ! SaveUser(userWorkspace)

    expectMsg(UserSaved)
  }

}
