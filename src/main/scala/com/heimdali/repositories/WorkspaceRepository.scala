package com.heimdali.repositories

import com.heimdali.models.ViewModel._
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait WorkspaceRepository {
  def list(username: String): Future[Seq[SharedWorkspace]]

  def create(name: String, purpose: String, phi: Boolean, pci: Boolean, pii: Boolean, requestedSizeInGB: Double): Future[SharedWorkspace]

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace]

  def setHDFS(id: Long, location: String): Future[SharedWorkspace]

  def setKeytab(id: Long, location: String): Future[SharedWorkspace]
}

case class SharedWorkspaceRecord(id: Long,
                                 name: String,
                                 purpose: String,
                                 phi: Boolean,
                                 pci: Boolean,
                                 pii: Boolean,
                                 requestedSizeInGB: Double,
                                 createdBy: String)

class WorkspaceRepositoryImpl(implicit ec: ExecutionContext)
  extends WorkspaceRepository {

  val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery

  import ctx._

  val projectQuery = query[SharedWorkspaceRecord]

  def list(username: String): Future[Seq[SharedWorkspace]] = run {
    projectQuery.filter(_.createdBy == lift(username))
  }.map(_.map { w =>
    SharedWorkspace(id)
  })

  def create(project: SharedWorkspace): Future[SharedWorkspace] = run {
    projectQuery.insert(lift(project)).returning(_.id)
  }.map(res => project.copy(id = res))

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace] =
    for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.ldapDn -> lift(Option(dn))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head

  def setHDFS(id: Long, location: String): Future[SharedWorkspace] =
    for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.hdfs.location -> lift(Option(location))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head

  def setKeytab(id: Long, location: String): Future[SharedWorkspace] =
    for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.keytabLocation -> lift(Option(location))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head

}
