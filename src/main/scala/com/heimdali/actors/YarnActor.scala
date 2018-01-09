package com.heimdali.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.heimdali.actors.WorkspaceSaver.ProjectUpdate
import com.heimdali.models.ViewModel.SharedWorkspace
import com.heimdali.services.{LoginContextProvider, YarnClient}

import scala.concurrent.ExecutionContext


object YarnActor {

  case class CreatePool(id: Long, poolName: String, maxCores: Int, maxMemoryInGB: Double)

  case class PoolCreated(id: Long, poolName: String) extends ProjectUpdate {
    override def updateProject(sharedWorkspace: SharedWorkspace): SharedWorkspace =
      sharedWorkspace.copy(yarn = sharedWorkspace.yarn.copy(poolName = Some(poolName)))
  }

}

class YarnActor(yarnClient: YarnClient)
               (implicit val executionContext: ExecutionContext) extends Actor {

  import YarnActor._

  override def receive: Receive = {
    case CreatePool(id, poolName, maxCores, maxMemoryInGB) =>
      (for (
        result <- yarnClient.createPool(poolName, maxCores, maxMemoryInGB)
      ) yield PoolCreated(id, result.name)).pipeTo(sender())
  }
}
