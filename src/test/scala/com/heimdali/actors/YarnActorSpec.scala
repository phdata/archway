package com.heimdali.actors

import javax.security.auth.Subject

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.heimdali.actors.YarnActor.{CreatePool, PoolCreated}
import com.heimdali.services.{LoginContextProvider, YarnClient, YarnPool}
import com.heimdali.test.fixtures.TestProject
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.Future

class YarnActorSpec extends FlatSpec with MockFactory {

  behavior of "Yarn actor"

  it should "request a pool" in new TestKit(ActorSystem()) {
    val project = TestProject()
    val probe = TestProbe()

    implicit val actorSystem = ActorSystem()

    val name = "sesame"
    val maxCores = 1
    val maxMemoryInGB = 1.0
    val config = ConfigFactory.load()
    val yarnPool = YarnPool(name, maxCores, maxMemoryInGB)

    val yarnClient = mock[YarnClient]
    implicit val executionContext = scala.concurrent.ExecutionContext.global
    (yarnClient.createPool _).expects(name, maxCores, maxMemoryInGB).returning(Future(yarnPool))

    val actor = actorSystem.actorOf(Props(classOf[YarnActor], yarnClient, executionContext))
    val request = CreatePool(project.id, project.systemName, project.yarn.maxCores, project.yarn.maxMemoryInGB)
    actor.tell(request, probe.ref)
    probe.expectMsgClass(classOf[PoolCreated])
  }
}
