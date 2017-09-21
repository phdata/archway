package com.heimdali.repositories

import javax.inject.Inject

import com.heimdali.models.Project
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait ProjectRepository {
  def list(username: String): Future[Seq[Project]]
  def create(project: Project): Future[Project]
  def setLDAP(id: Long, name: String, dn: String): Future[Project]
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

  def setLDAP(id: Long, name: String, dn: String): Future[Project] =
    for (
      _ <- run(query[Project].filter(_.id == lift(id)).update(_.systemName -> lift(name), _.ldapDn -> lift(Option(dn))));
      project <- run(query[Project].filter(_.id == lift(id)))
    ) yield project.head

}