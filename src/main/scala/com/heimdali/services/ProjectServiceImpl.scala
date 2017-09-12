package com.heimdali.services

import javax.inject.Inject

import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.Future

trait ProjectService {
  def create(project: Project): Future[Project]
  def list(username: String): Future[Seq[Project]]
}

class ProjectServiceImpl @Inject()(projectRepository: ProjectRepository) extends ProjectService {
  override def list(username: String): Future[Seq[Project]] = projectRepository.list(username)

  override def create(project: Project): Future[Project] = projectRepository.create(project)

}