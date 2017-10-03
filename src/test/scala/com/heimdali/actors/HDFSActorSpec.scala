package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.actors.HDFSActor.{CreateDirectory, HDFSDone}
import com.heimdali.services.{HDFSAllocation, HDFSClient}
import com.heimdali.test.fixtures.TestProject
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import play.api.Configuration

import scala.concurrent.Future

class HDFSActorSpec extends FlatSpec with MockFactory {

  behavior of "HDFS Actor"

  it should "request a new project created" in {
    val project = TestProject()

    implicit val actorSystem = ActorSystem()
    val probe = TestProbe()
    val location = s"/projects/${project.systemName}"
    val path = new Path(location)
    val config = Configuration(ConfigFactory.load())

    val ldapClient = mock[HDFSClient]
    implicit val executionContext = scala.concurrent.ExecutionContext.global
    (ldapClient.createDirectory _).expects(location).returning(Future(path))
    (ldapClient.setQuota _).expects(path, 10).returning(Future(HDFSAllocation(location, 10)))

    val actor = actorSystem.actorOf(Props(classOf[HDFSActor], ldapClient, config, executionContext))
    val request = CreateDirectory(project.systemName, project.hdfs.requestedSizeInGB)
    actor.tell(request, probe.ref)
    probe.expectMsgClass(classOf[HDFSDone])
  }

}
