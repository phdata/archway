package com.heimdali.repositories

import com.heimdali.models.Project

import scala.concurrent.Future

trait ProjectRepository {

  def create(project: Project): Future[Project]

}
