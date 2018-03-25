package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.actors.HiveActor.{CreateSharedDatabase, CreateUserDatabase, DatabaseCreated}
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class HiveActorSpec extends FlatSpec with MockitoSugar with Matchers {

  behavior of "Hive Actor"

  it should "create a new db" in new TestKit(ActorSystem()) with ImplicitSender {
    val username = "username"

    val service = new HappyHiveService
    val config = ConfigFactory.load()
    val hadoopConfiguration = new Configuration(false)

    val actor = system.actorOf(Props(classOf[HiveActor], config, hadoopConfiguration, service, ExecutionContext.global))

    actor ! CreateUserDatabase(username)

    expectMsgPF() {
      case DatabaseCreated(HiveDatabase(location, role, database)) =>
        database should be (s"user_$username")
        location should be (s"/users/$username/db")
        role should be (s"role_$username")
    }
  }

  it should "create a new project db" in new TestKit(ActorSystem()) with ImplicitSender {
    val username = "myproject"

    val service = new HappyHiveService
    val config = ConfigFactory.load()
    val hadoopConfiguration = new Configuration(false)

    val actor = system.actorOf(Props(classOf[HiveActor], config, hadoopConfiguration, service, ExecutionContext.global))

    actor ! CreateSharedDatabase(username)

    expectMsgType[DatabaseCreated]
  }

}