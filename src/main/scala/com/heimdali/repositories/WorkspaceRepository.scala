package com.heimdali.repositories

import java.time.{LocalDateTime, ZoneOffset}

import cats.effect.IO
import com.heimdali.models.ViewModel._
import doobie._
import doobie.implicits._

import scala.concurrent.{ExecutionContext, Future}

trait WorkspaceRepository {
  def list(username: String): Future[Seq[SharedWorkspace]]

  def create(sharedWorkspace: SharedWorkspaceRequest): Future[SharedWorkspace]

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace]

  def setHDFS(id: Long, location: String, actualGB: Double): Future[SharedWorkspace]

  def setKeytab(id: Long, location: String): Future[SharedWorkspace]

  def setYarn(id: Long, poolName: String): Future[SharedWorkspace]
}

class WorkspaceRepositoryImpl(transactor: Transactor[IO])
                             (implicit ec: ExecutionContext)
  extends WorkspaceRepository {

  implicit val DateTimeMeta: Meta[LocalDateTime] =
    Meta[java.sql.Timestamp].nxmap(
      ts => LocalDateTime.ofEpochSecond(ts.getTime, ts.getNanos, ZoneOffset.UTC),
      dt => new java.sql.Timestamp(dt.toEpochSecond(ZoneOffset.UTC))
    )

  def find(id: Long): ConnectionIO[SharedWorkspace] =
    sql"""
         | SELECT
         |   id,
         |   name,
         |   purpose,
         |   ldap_dn,
         |   system_name,
         |   pii_data,
         |   pci_data,
         |   phi_data,
         |   hdfs_location,
         |   hdfs_requested_size_in_gb,
         |   hdfs_actual_size_in_gb,
         |   yarn_pool_name,
         |   yarn_max_cores,
         |   yarn_max_memory_in_gb,
         |   keytab_location,
         |   created_by,
         |   created
         | FROM
         |   shared_workspaces
         | WHERE
         |   id = $id
        """.stripMargin.query[SharedWorkspace].unique

  def list(username: String): Future[Seq[SharedWorkspace]] =
    sql"""
         | SELECT
         |   id,
         |   name,
         |   purpose,
         |   ldap_dn,
         |   system_name,
         |   pii_data,
         |   pci_data,
         |   phi_data,
         |   hdfs_location,
         |   hdfs_requested_size_in_gb,
         |   hdfs_actual_size_in_gb,
         |   yarn_pool_name,
         |   yarn_max_cores,
         |   yarn_max_memory_in_gb,
         |   keytab_location,
         |   created_by,
         |   created
         | FROM
         |   shared_workspaces
         | WHERE
         |   created_by = $username
      """.stripMargin
      .query[SharedWorkspace]
      .to[Seq]
      .transact(transactor)
      .unsafeToFuture()

  def create(sharedWorkspace: SharedWorkspaceRequest): Future[SharedWorkspace] = {
    val result = for (
      id <- sql"""
                 | INSERT INTO
                 |   shared_workspaces (
                 |     name,
                 |     purpose,
                 |     pii_data,
                 |     pci_data,
                 |     phi_data,
                 |     requested_size_in_gb,
                 |     max_cores,
                 |     max_memory_in_gb,
                 |     created_by
                 |   )
                 | VALUES (
                 |   ${sharedWorkspace.name},
                 |   ${sharedWorkspace.purpose},
                 |   ${sharedWorkspace.compliance.piiData},
                 |   ${sharedWorkspace.compliance.pciData},
                 |   ${sharedWorkspace.compliance.phiData},
                 |   ${sharedWorkspace.hdfs.requestedSizeInGB},
                 |   ${sharedWorkspace.yarn.maxCores},
                 |   ${sharedWorkspace.yarn.maxMemoryInGB},
                 |   ${sharedWorkspace.createdBy}
                 | )
      """.stripMargin.update.withUniqueGeneratedKeys[Long]("id");
      record <- find(id)
    ) yield record

    result
      .transact(transactor)
      .unsafeToFuture()
  }

  def setLDAP(id: Long, dn: String): Future[SharedWorkspace] =
    (for (
      _ <- sql"UPDATE shared_workspaces SET ldap_dn = $dn WHERE id = $id".update.run;
      record <- find(id)
    ) yield record)
      .transact(transactor)
      .unsafeToFuture()

  def setHDFS(id: Long, location: String, actualGB: Double): Future[SharedWorkspace] =
    (for (
      _ <- sql"UPDATE shared_workspaces SET hdfs_location = $location, hdfs_actual_size_in_gb = $actualGB WHERE id = $id".update.run;
      record <- find(id)
    ) yield record)
      .transact(transactor)
      .unsafeToFuture()

  def setKeytab(id: Long, location: String): Future[SharedWorkspace] =
    (for (
      _ <- sql"UPDATE shared_workspaces SET keytab_location = $location WHERE id = $id".update.run;
      record <- find(id)
    ) yield record)
      .transact(transactor)
      .unsafeToFuture()

  def setYarn(id: Long, poolName: String): Future[SharedWorkspace] =
    (for (
      _ <- sql"UPDATE shared_workspaces SET yarn_pool_name = $poolName WHERE id = $id".update.run;
      record <- find(id)
    ) yield record)
      .transact(transactor)
      .unsafeToFuture()

}
