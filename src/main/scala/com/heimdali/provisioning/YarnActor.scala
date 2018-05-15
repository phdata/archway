package com.heimdali.provisioning

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.heimdali.clients.YarnClient

import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext
import cats._

object YarnActor {

    case class CreatePool(parentPools: Queue[String], poolName: String, maxCores: Int, maxMemoryInGB: Double)

  case class PoolCreated(poolName: String)

  def props(yarnClient: YarnClient)
           (implicit executionContext: ExecutionContext): Props =
    Props(classOf[YarnActor], yarnClient, executionContext)

}

class YarnActor(yarnClient: YarnClient)
               (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import YarnActor._

  override def receive: Receive = {
    case CreatePool(parentPools, poolName, maxCores, maxMemoryInGB) =>
      (for (
        result <- yarnClient.createPool(poolName, maxCores, maxMemoryInGB, parentPools)
      ) yield PoolCreated(result.name))
          .recover {
            case exc =>
              log.error(exc, "Failed to set up yarn: {}", exc.toString)
          }
        .pipeTo(sender())
  }
}
