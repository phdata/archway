package com.heimdali.repositories

import javax.inject.Inject

import com.heimdali.models.Project
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait ProjectRepository {

  def create(project: Project): Future[Project]

}

class ProjectRepositoryImpl @Inject() (implicit ec: ExecutionContext)
  extends ProjectRepository {

  val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery

  import ctx._

  def create(project: Project): Future[Project] = run {
    query[Project].insert(lift(project)).returning(_.id)
  }.map(res => project.copy(id = res))

}