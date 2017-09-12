package com.heimdali.repositories

import javax.inject.Inject

import com.heimdali.models.Project
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait ProjectRepository {
  def list(username: String): Future[Seq[Project]]
  def create(project: Project): Future[Project]

}

class ProjectRepositoryImpl @Inject() (implicit ec: ExecutionContext)
  extends ProjectRepository {

  val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery

  import ctx._

  def list(username: String): Future[Seq[Project]] = run {
    query[Project].filter(_.createdBy == lift(username))
  }

  def create(project: Project): Future[Project] = run {
    query[Project].insert(lift(project)).returning(_.id)
  }.map(res => project.copy(id = res))

}