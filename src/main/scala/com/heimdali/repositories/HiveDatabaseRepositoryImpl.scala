package com.heimdali.repositories

import java.time.{Clock, Instant}

import cats._, cats.data._, cats.implicits._
import com.heimdali.models.HiveDatabase
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class HiveDatabaseRepositoryImpl(clock: Clock)
    extends HiveDatabaseRepository {

  implicit val han = LogHandler.jdkLogHandler

  override def complete(id: Long): ConnectionIO[Int] =
    sql"""
      update hive_database
      set created = ${Instant.now()}
      where id = $id
      """.update.run

  def insert(hiveDatabase: HiveDatabase): ConnectionIO[Long] =
    sql"""
       insert into hive_database (name, location, size_in_gb, workspace_request_id, manager_group_id, readonly_group_id)
       values(
        ${hiveDatabase.name},
        ${hiveDatabase.location},
        ${hiveDatabase.sizeInGB},
        ${hiveDatabase.workspaceRequestId},
        ${hiveDatabase.managingGroup.id},
        ${hiveDatabase.readonlyGroup.id}
       )
      """.updateWithLogHandler(LogHandler.jdkLogHandler).withUniqueGeneratedKeys[Long]("id")

  val selectQuery: Fragment =
    sql"""
       select
         h.name,
         h.location,
         h.size_in_gb,
         m.distinguished_name,
         m.common_name,
         m.sentry_role,
         m.id,
         m.group_created,
         m.role_created,
         m.group_associated,
         r.distinguished_name,
         r.common_name,
         r.sentry_role,
         r.id,
         r.group_created,
         r.role_created,
         r.group_associated,
         h.workspace_request_id,
         h.id,
         h.directory_created,
         h.database_created,
         h.quota_set,
         h.manager_location_access,
         h.manager_db_access,
         h.readonly_location_access,
         h.readonly_db_access
       from hive_database h
       inner join ldap_registration m on h.manager_group_id = m.id
       left join ldap_registration r on h.readonly_group_id = r.id
      """

  def find(id: Long): OptionT[ConnectionIO, HiveDatabase] = {
    OptionT {
      (selectQuery ++ whereAnd(fr"h.id = $id")).query[HiveDatabase].option
    }
  }

  override def create(hiveDatabase: HiveDatabase): ConnectionIO[HiveDatabase] =
    for {
      id <- insert(hiveDatabase)
      hive <- find(id).value
    } yield hive.get

  override def findByWorkspace(id: Long): ConnectionIO[List[HiveDatabase]] =
    (selectQuery ++ whereAnd(fr"h.workspace_request_id = $id"))
      .query[HiveDatabase].to[List]

  override def directoryCreated(id: Long): ConnectionIO[Int] =
    sql"update hive_database set directory_created = ${Instant.now(clock)} where id = $id".update.run

  override def quotaSet(id: Long): ConnectionIO[Int] =
    sql"update hive_database set quota_set = ${Instant.now(clock)} where id = $id".update.run

  override def databaseCreated(id: Long): ConnectionIO[Int] =
    sql"update hive_database set database_created = ${Instant.now(clock)} where id = $id".update.run

}
