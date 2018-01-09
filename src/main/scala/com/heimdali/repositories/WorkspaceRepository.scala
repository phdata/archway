package com.heimdali.repositories

import java.time.LocalDateTime

import com.heimdali.models.ViewModel._
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait WorkspaceRepository {
  def list(username: String): Future[Seq[SharedWorkspace]]

  def create(sharedWorkspace: SharedWorkspaceRequest): Future[SharedWorkspace]

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace]

  def setHDFS(id: Long, location: String, actualGB: Double): Future[SharedWorkspace]

  def setKeytab(id: Long, location: String): Future[SharedWorkspace]

  def setYarn(id: Long, poolName: String): Future[SharedWorkspace]
}

case class SharedWorkspaceRecord(id: Long,
                                 name: String,
                                 purpose: String,
                                 createdBy: String,
                                 created: LocalDateTime,
                                 systemName: String,
                                 ldapDn: Option[String],
                                 phiData: Boolean,
                                 pciData: Boolean,
                                 piiData: Boolean,
                                 hdfsLocation: Option[String],
                                 hdfsRequestedSizeInGb: Double,
                                 hdfsActualSizeInGb: Option[Double],
                                 keytabLocation: Option[String],
                                 yarnPoolName: Option[String],
                                 yarnMaxCores: Int,
                                 yarnMaxMemoryInGb: Double)

object SharedWorkspaceRecord {
  def apply(sharedWorkspace: SharedWorkspaceRequest): SharedWorkspaceRecord =
    SharedWorkspaceRecord(
      123L,
      sharedWorkspace.name,
      sharedWorkspace.purpose,
      sharedWorkspace.createdBy.get,
      LocalDateTime.now(),
      SharedWorkspace.generateName(sharedWorkspace.name),
      None,
      sharedWorkspace.compliance.phiData,
      sharedWorkspace.compliance.pciData,
      sharedWorkspace.compliance.piiData,
      None,
      sharedWorkspace.hdfs.requestedSizeInGB,
      None,
      None,
      None,
      sharedWorkspace.yarn.maxCores,
      sharedWorkspace.yarn.maxMemoryInGB)
}

class WorkspaceRepositoryImpl(implicit ec: ExecutionContext)
  extends WorkspaceRepository {

  val ctx = new PostgresAsyncContext(NamingStrategy(SnakeCase, PluralizedTableNames), "ctx") with ImplicitQuery

  import ctx._

  val projectQuery = quote {
    querySchema[SharedWorkspaceRecord](
      "projects"
    )
  }

  def list(username: String): Future[Seq[SharedWorkspace]] = run(quote {
    projectQuery.filter(_.createdBy == lift(username))
  }).map(_.map(SharedWorkspace.apply))

  def create(sharedWorkspace: SharedWorkspaceRequest): Future[SharedWorkspace] = {
    val workspace = SharedWorkspaceRecord(sharedWorkspace)
    run {
      projectQuery.insert(lift(workspace)).returning(_.id)
    }
      .map(res => workspace.copy(id = res))
      .map(SharedWorkspace.apply)
      .recover {
        case e =>
          throw e
      }
  }

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace] =
    (for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.ldapDn -> lift(Option(dn))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head)
      .map(SharedWorkspace.apply)

  def setHDFS(id: Long, location: String, actualGB: Double): Future[SharedWorkspace] =
    (for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.hdfsLocation -> lift(Option(location)), _.hdfsActualSizeInGb -> lift(Option(actualGB))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head)
      .map(SharedWorkspace.apply)

  def setKeytab(id: Long, location: String): Future[SharedWorkspace] =
    (for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.keytabLocation -> lift(Option(location))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head)
      .map(SharedWorkspace.apply)

  def setYarn(id: Long, poolName: String): Future[SharedWorkspace] =
    (for (
      _ <- run(projectQuery.filter(_.id == lift(id)).update(_.yarnPoolName -> lift(Option(poolName))));
      project <- run(projectQuery.filter(_.id == lift(id)))
    ) yield project.head)
      .map(SharedWorkspace.apply)

}
