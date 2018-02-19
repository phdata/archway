package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.actors.HiveActor.{CreateUserDatabase, UserDatabaseCreated}
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import scalikejdbc.DBSession

import scala.concurrent.ExecutionContext

class HiveActorSpec extends FlatSpec with MockitoSugar with Matchers {

  behavior of "Hive Actor"

  it should "create a new db" in new TestKit(ActorSystem()) with ImplicitSender {
    val username = "username"
    val role = s"role_$username"
    val location = s"/users/$username/db"
    val database = s"user_$username"

    val session = mock[DBSession]
    when(session.execute(anyString, ArgumentMatchers.anyString))
      .thenReturn(true)

    val config = ConfigFactory.load()

    val actor = system.actorOf(Props(classOf[HiveActor], config, session, ExecutionContext.global))

    actor ! CreateUserDatabase(username)

    expectMsgPF() {
      case UserDatabaseCreated(HiveDatabase(`location`, `role`, `database`)) =>
        database should be (s"user_$username")
        location should be (s"/users/$username/db")
        role should be (s"role_$username")
    }
  }

}
