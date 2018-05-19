package com.heimdali.provisioning

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.clients.{YarnClient, YarnPool}
import com.heimdali.provisioning.YarnActor.{CreatePool, PoolCreated}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import com.heimdali.test.fixtures._

import scala.collection.immutable.Queue
import scala.concurrent.Future

class YarnActorSpec extends FlatSpec with MockFactory {

  behavior of "Yarn actor"

  it should "request a pool" in new TestKit(ActorSystem()) with ImplicitSender {
    implicit val actorSystem = ActorSystem()

    val config = ConfigFactory.load()
    val yarnPool = YarnPool(name, maxCores, maxMemoryInGB)
    val parents = Queue("root")

    val yarnClient = mock[YarnClient]
    implicit val executionContext = scala.concurrent.ExecutionContext.global
    yarnClient.createPool _ expects(poolName, maxCores, maxMemoryInGB, parents) returning Future(yarnPool)

    val actor = actorSystem.actorOf(YarnActor.props(yarnClient))
    val request = CreatePool(parents, yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB)
    actor.tell(request, testActor)
    expectMsgClass(classOf[PoolCreated])
  }
}
