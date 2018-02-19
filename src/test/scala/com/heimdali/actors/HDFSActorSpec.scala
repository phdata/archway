package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.heimdali.actors.HDFSActor.{CreateSharedDirectory, HDFSUpdate}
import com.heimdali.services.HDFSClient
import com.heimdali.test.fixtures.TestProject
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.{ExecutionContext, Future}

class HDFSActorSpec extends FlatSpec with MockFactory {

  behavior of "HDFS Actor"

  it should "request a new project created" in new TestKit(ActorSystem()) {
    implicit val executiionContext = ExecutionContext.global
    val config = ConfigFactory.load()
    val project = TestProject()

    val location = s"/projects/${project.systemName}"
    val path = new Path(location)

    val hdfsClient = mock[HDFSClient]
    (hdfsClient.createDirectory _).expects(location).returning(Future(path))

    val actor = system.actorOf(Props(classOf[HDFSActor], hdfsClient, config, executiionContext))
    val request = CreateDirectory(project.id, project.systemName, project.hdfs.requestedSizeInGB)
    actor.tell(request, testActor)
    expectMsgClass(classOf[HDFSUpdate])
  }

}
