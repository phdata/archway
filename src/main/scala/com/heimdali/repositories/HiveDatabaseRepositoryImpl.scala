package com.heimdali.repositories

import java.time.{Clock, Instant}

import cats._, cats.data._, cats.implicits._
import com.heimdali.models.HiveDatabase
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class HiveDatabaseRepositoryImpl(clock: Clock)
    extends HiveDatabaseRepository {

  override def complete(id: Long): ConnectionIO[Int] =
    sql"""
      update hive_database
      set created = ${Instant.now()}
      where id = $id
      """.update.run

  def insert(hiveDatabase: HiveDatabase): ConnectionIO[Long] =
    sql"""
       insert into hive_database (name, location, size_in_gb, manager_group_id, readonly_group_id)
       values(
        ${hiveDatabase.name},
        ${hiveDatabase.location},
        ${hiveDatabase.sizeInGB},
        ${hiveDatabase.managingGroup.id},
        ${hiveDatabase.readonlyGroup.flatMap(_.id)}
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
         m.created,
         r.distinguished_name,
         r.common_name,
         r.sentry_role,
         r.id,
         r.created,
         h.id
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
    (selectQuery ++
      fr"inner join request_hive rh on rh.hive_database_id = h.id" ++
      fr"inner join workspace_request w on rh.workspace_request_id = w.id" ++
      whereAnd(fr"w.id = $id")).query[HiveDatabase].to[List]

  override def directoryCreated(id: Long): ConnectionIO[Int] =
    sql"update hive_database set directory_created = ${Instant.now(clock)} where id = $id".update.run

  override def quotaSet(id: Long): ConnectionIO[Int] =
    sql"update hive_database set quota_set = ${Instant.now(clock)} where id = $id".update.run

  override def databaseCreated(id: Long): ConnectionIO[Int] =
    sql"update hive_database set database_created = ${Instant.now(clock)} where id = $id".update.run

  override def locationGranted(role: DatabaseRole, id: Long): ConnectionIO[Int] =
    (fr"update hive_database set " ++ Fragment.const(s"${role.show}_location_access") ++ fr" = ${Instant.now(clock)} where id = $id").update.run

  override def databaseGranted(role: DatabaseRole, id: Long): ConnectionIO[Int] =
    (fr"update hive_database set " ++ Fragment.const(s"${role.show}_db_access") ++ fr" = ${Instant.now(clock)} where id = $id").update.run

}
