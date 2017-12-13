package com.heimdali.repositories

import javax.inject.Inject

import com.heimdali.models.ViewModel._
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait WorkspaceRepository {
  def list(username: String): Future[Seq[SharedWorkspace]]

  def create(project: SharedWorkspace): Future[SharedWorkspace]

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace]

  def setHDFS(id: Long, location: String): Future[SharedWorkspace]

  def setKeytab(id: Long, location: String): Future[SharedWorkspace]
}

class WorkspaceRepositoryImpl @Inject()(implicit ec: ExecutionContext)
  extends WorkspaceRepository {

  val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery

  import ctx._

  val projectQuery = quote {
    querySchema[SharedWorkspace](
      "projects",
      _.compliance.pciData -> "pci_data",
      _.compliance.phiData -> "phi_data",
      _.compliance.piiData -> "pii_data",
      _.hdfs.location -> "hdfs_location",
      _.hdfs.requestedSizeInGB -> "hdfs_requested_size_in_gb"
    )
  }

  def list(username: String): Future[Seq[SharedWorkspace]] = run {
    projectQuery.filter(_.createdBy == lift(username))
  }

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
