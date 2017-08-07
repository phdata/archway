package com.heimdali.services

import javax.inject.Inject

import com.heimdali.models.Project
import com.heimdali.repositories.ProjectRepository

import scala.concurrent.Future

trait ProjectService {
  def create(project: Project): Future[Project]
}

class ProjectServiceImpl @Inject()(projectRepository: ProjectRepository) extends ProjectService {

  override def create(project: Project): Future[Project] = projectRepository.create(project)

}