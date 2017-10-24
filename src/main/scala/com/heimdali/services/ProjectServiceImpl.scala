package com.heimdali.services

import javax.inject.Inject

import akka.actor.{ActorSystem, Props}
import com.heimdali.actors.ProjectProvisioner
import com.heimdali.actors.ProjectProvisioner.Request
import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait ProjectService {
  def create(project: Project): Future[Project]

  def list(username: String): Future[Seq[Project]]
}

class ProjectServiceImpl @Inject()(projectRepository: ProjectRepository,
                                   provisioningFactory: ProjectProvisioner.Factory)
                                  (implicit val executionContext: ExecutionContext,
                                   val actorSystem: ActorSystem) extends ProjectService {
  override def list(username: String): Future[Seq[Project]] = projectRepository.list(username)

  override def create(project: Project): Future[Project] = {
    projectRepository.create(project) map { updated =>
      val actorRef = actorSystem.actorOf(Props(provisioningFactory(updated)), updated.name)
      actorSystem.scheduler.scheduleOnce(0 seconds, actorRef, Request)
      updated
    }
  }

}