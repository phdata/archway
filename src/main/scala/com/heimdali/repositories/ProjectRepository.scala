package com.heimdali.repositories

import javax.inject.Inject

import com.heimdali.models.Project
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait ProjectRepository {
  def list(username: String): Future[Seq[Project]]

  def create(project: Project): Future[Project]

  def setLDAP(id: Long, dn: String): Future[Project]

  def setHDFS(id: Long, location: String): Future[Project]

  def setKeytab(id: Long, location: String): Future[Project]
}

class ProjectRepositoryImpl @Inject()(implicit ec: ExecutionContext)
  extends ProjectRepository {

  val ctx = new PostgresAsyncContext[SnakeCase with PluralizedTableNames]("ctx") with ImplicitQuery

  import ctx._

  val projectQuery = quote {
    querySchema[Project](
      "projects",
      _.compliance.pciData -> "pci_data",
      _.compliance.phiData -> "phi_data",
      _.compliance.piiData -> "pii_data",
      _.hdfs.location -> "hdfs_location",
      _.hdfs.requestedSizeInGB -> "hdfs_requested_size_in_gb"
    )
  }

  def list(username: String): Future[Seq[Project]] = run {
    projectQuery.filter(_.createdBy == lift(username))
  }

  def create(project: Project): Future[Project] = run {
    projectQuery.insert(lift(project)).returning(_.id)
  }.map(res => project.copy(id = res))

  def setLDAP(id: Long, dn: String): Future[Project] =
    for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.ldapDn -> lift(Option(dn))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head

  def setHDFS(id: Long, location: String): Future[Project] =
    for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.hdfs.location -> lift(Option(location))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head

  def setKeytab(id: Long, location: String): Future[Project] =
    for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.keytabLocation -> lift(Option(location))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head

}
