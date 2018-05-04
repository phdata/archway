package com.heimdali.provisioning

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.clients.HDFSClient
import com.heimdali.provisioning.HDFSActor.{CreateDirectory, DirectoryCreated}
import com.heimdali.services.LoginContextProvider
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.{ExecutionContext, Future}

class HDFSActorSpec extends FlatSpec with MockFactory {

  behavior of "HDFS Actor"

  it should "request a new project created" in new TestKit(ActorSystem()) with ImplicitSender {
    import com.heimdali.test.fixtures._

    implicit val executiionContext = ExecutionContext.global
    val config = ConfigFactory.load()
    val provider = mock[LoginContextProvider]

    val path = new Path(hdfsLocation)

    val hdfsClient = mock[HDFSClient]
    (hdfsClient.createDirectory _).expects(hdfsLocation, *).returning(Future(path))

    val actor = system.actorOf(Props(classOf[HDFSActor], hdfsClient, provider, config, executiionContext))
    val request = CreateDirectory(hdfsLocation, hdfsRequestedSize, None)
    actor ! request
    expectMsgClass(classOf[DirectoryCreated])
  }

}