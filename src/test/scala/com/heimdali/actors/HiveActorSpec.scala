package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.actors.HiveActor.{CreateSharedDatabase, CreateUserDatabase, DatabaseCreated}
import com.heimdali.services.HiveService
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class HiveActorSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "Hive Actor"

  it should "create a new db" in new TestKit(ActorSystem()) with ImplicitSender {
    val username = "username"

    val service = mock[HiveService]
    inSequence {
      service.createRole _ expects s"role_$username" returning Future(Some(1))
      service.grantGroup _ expects(s"edh_user_$username", s"role_$username") returning Future(0)
      service.createDatabase _ expects(s"user_$username", s"/users/$username/db") returning Future(Some(1))
      service.enableAccessToDB _ expects(s"user_$username", s"role_$username") returning Future(1)
      service.enableAccessToLocation _ expects(s"/users/$username/db", s"role_$username") returning Future(0)
    }

    val config = ConfigFactory.load()
    val hadoopConfiguration = new Configuration(false)

    val actor = system.actorOf(Props(classOf[HiveActor], config, hadoopConfiguration, service, ExecutionContext.global))

    actor ! CreateUserDatabase(username)

    expectMsgPF() {
      case DatabaseCreated(HiveDatabase(location, role, database)) =>
        database should be(s"user_$username")
        location should be(s"/users/$username/db")
        role should be(s"role_$username")
    }
  }

  it should "create a new project db" in new TestKit(ActorSystem()) with ImplicitSender {
    val project = "myproject"

    val service = mock[HiveService]
    inSequence {
      service.createRole _ expects s"role_sw_$project" returning Future(Some(1))
      service.grantGroup _ expects(s"edh_sw_$project", s"role_sw_$project") returning Future(0)
      service.createDatabase _ expects(s"sw_$project", s"/data/shared_workspaces/$project") returning Future(Some(1))
      service.enableAccessToDB _ expects(s"sw_$project", s"role_sw_$project") returning Future(1)
      service.enableAccessToLocation _ expects(s"/data/shared_workspaces/$project", s"role_sw_$project") returning Future(0)
    }

    val config = ConfigFactory.load()
    val hadoopConfiguration = new Configuration(false)

    val actor = system.actorOf(Props(classOf[HiveActor], config, hadoopConfiguration, service, ExecutionContext.global))

    actor ! CreateSharedDatabase(project)

    expectMsgType[DatabaseCreated]
  }

}