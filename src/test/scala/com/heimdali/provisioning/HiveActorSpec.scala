package com.heimdali.provisioning

import java.sql.Connection

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.models.HiveDatabase
import com.heimdali.provisioning.HiveActor.{CreateDatabase, DatabaseCreated}
import com.heimdali.services.HiveService
import com.heimdali.test.fixtures._
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class HiveActorSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "Hive Actor"

  it should "create a new db" in new TestKit(ActorSystem()) with ImplicitSender {

    val service = mock[HiveService]
    val connection = mock[Connection]
    inSequence {
      (service.createRole(_: String)(_: Connection))
        .expects(hive.role, connection)
        .returning(Future(Some(true)))

      (service.grantGroup(_: String, _: String)(_: Connection))
        .expects(ldap.commonName, hive.role, connection)
        .returning(Future(true))

      (service.createDatabase(_: String, _: String)(_: Connection))
        .expects(hive.name, hive.location, connection)
        .returning(Future(Some(true)))

      (service.enableAccessToDB(_: String, _: String)(_: Connection))
        .expects(hive.name, hive.role, connection)
        .returning(Future(true))

      (service.enableAccessToLocation(_: String, _: String)(_: Connection))
        .expects(hive.location, hive.role, connection)
        .returning(Future(true))
    }

    val config = ConfigFactory.load()
    val hadoopConfiguration = new Configuration(false)

    val actor = system.actorOf(Props(classOf[HiveActor],
      config,
      hadoopConfiguration,
      service,
      () => connection,
      ExecutionContext.global))

    actor.tell(CreateDatabase(ldap.commonName,
      hive.name,
      hive.role,
      hive.location),
      testActor)

    expectMsg(DatabaseCreated(HiveDatabase(None, hive.name, hive.role, hive.location, 0)))
  }

}