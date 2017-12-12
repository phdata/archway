package com.heimdali.services

import akka.actor.{ActorRef, ActorSystem}
import com.heimdali.actors.ProjectProvisioner.Request
import com.heimdali.Model._
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {
  def create(project: Project): Future[Project]

  def list(username: String): Future[Seq[Project]]
}

class ProjectServiceImpl(projectRepository: ProjectRepository,
                         provisioningFactory: Project => ActorRef)
                        (implicit executionContext: ExecutionContext,
                         actorSystem: ActorSystem) extends ProjectService {
  override def list(username: String): Future[Seq[Project]] = projectRepository.list(username)

  override def create(project: Project): Future[Project] = {
    projectRepository.create(project) map { updated =>
      actorSystem.scheduler.scheduleOnce(0 seconds, provisioningFactory(project), Request)
      updated
    }
  }

}