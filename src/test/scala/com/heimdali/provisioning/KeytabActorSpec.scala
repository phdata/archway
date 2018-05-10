package com.heimdali.provisioning

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.heimdali.provisioning.KeytabActor.{GenerateKeytab, KeytabCreated}
import com.heimdali.clients.HDFSClient
import com.heimdali.services.KeytabService
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.fs.Path
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class KeytabActorSpec extends FlatSpec with MockFactory with Matchers {

  behavior of "Keytab actor"

  ignore should "generate and upload a keytab" in new TestKit(ActorSystem()) {

    import ExecutionContext.Implicits.global

    val hdfsClient = mock[HDFSClient]
    val keytabService = mock[KeytabService]

    val principal = "project"
    val location = new Path("/projects/project")
    val keytabContent = "keytab content"
    val fullPath = new Path("/data/shared_workspaces/project/project.keytab")
    val configuration = ConfigFactory.load()

    (keytabService.generateKeytab _).expects(principal).returning(Future(keytabContent))
    (hdfsClient.uploadFile _).expects(*, fullPath).returning(Future(fullPath))

    val actor = system.actorOf(KeytabActor.props(hdfsClient, keytabService, configuration))

    actor.tell(GenerateKeytab(principal), testActor)

    expectMsg(KeytabCreated(fullPath))
  }

}