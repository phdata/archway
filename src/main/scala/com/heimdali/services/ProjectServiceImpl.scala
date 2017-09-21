package com.heimdali.services

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import com.heimdali.actors.ProjectProvisioner.Request
import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait ProjectService {
  def create(project: Project): Future[Project]

  def list(username: String): Future[Seq[Project]]
}

class ProjectServiceImpl @Inject()(projectRepository: ProjectRepository,
                                   @Named("provisioning-actor") provisionActor: ActorRef)
                                  (implicit val executionContext: ExecutionContext,
                                   val actorSystem: ActorSystem) extends ProjectService {
  override def list(username: String): Future[Seq[Project]] = projectRepository.list(username)

  override def create(project: Project): Future[Project] = {
    projectRepository.create(project) map { updated =>
      actorSystem.scheduler.scheduleOnce(0 seconds, provisionActor, Request(updated))
      updated
    }
  }

}