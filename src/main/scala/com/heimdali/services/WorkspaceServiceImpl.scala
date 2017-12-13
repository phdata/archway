package com.heimdali.services

import akka.actor.{ActorRef, ActorSystem}
import com.heimdali.actors.WorkspaceProvisioner.Request
import com.heimdali.models.ViewModel._
import com.heimdali.repositories.WorkspaceRepository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait WorkspaceService {
  def create(workspace: SharedWorkspace): Future[SharedWorkspace]

  def list(username: String): Future[Seq[SharedWorkspace]]
}

class WorkspaceServiceImpl(workspaceRepository: WorkspaceRepository,
                           provisioningFactory: SharedWorkspace => ActorRef)
                          (implicit executionContext: ExecutionContext,
                           actorSystem: ActorSystem) extends WorkspaceService {
  override def list(username: String): Future[Seq[SharedWorkspace]] = workspaceRepository.list(username)

  override def create(workspace: SharedWorkspace): Future[SharedWorkspace] = {
    workspaceRepository.create(workspace) map { updated =>
      actorSystem.scheduler.scheduleOnce(0 seconds, provisioningFactory(workspace), Request)
      updated
    }
  }

}