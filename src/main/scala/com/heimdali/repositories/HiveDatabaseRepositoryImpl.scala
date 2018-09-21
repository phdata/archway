package com.heimdali.repositories

import java.time.{Clock, Instant}

import cats._, cats.data._, cats.implicits._
import com.heimdali.models.HiveDatabase
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class HiveDatabaseRepositoryImpl(val clock: Clock)
  extends HiveDatabaseRepository {

  def find(id: Long): OptionT[ConnectionIO, HiveDatabase] = {
    OptionT {
      Statements
        .find(id)
        .option
    }
  }

  override def create(hiveDatabase: HiveDatabase): ConnectionIO[Long] =
    Statements
      .insert(hiveDatabase)
      .withUniqueGeneratedKeys("id")

  override def findByWorkspace(id: Long): ConnectionIO[List[HiveDatabase]] =
    Statements
      .list(id)
      .to[List]

  override def directoryCreated(id: Long): ConnectionIO[Int] =
    Statements
      .directoryCreated(id)
      .run

  override def quotaSet(id: Long): ConnectionIO[Int] =
    Statements
      .quotaSet(id)
      .run

  override def databaseCreated(id: Long): ConnectionIO[Int] =
    Statements
      .databaseCreated(id)
      .run

  object Statements {

    val selectQuery: Fragment =
      sql"""
       select
         h.name,
         h.location,
         h.size_in_gb,
         CAST(0.0 as FLOAT),

         h.name,
         h.location,
         m.distinguished_name,
         m.common_name,
         m.sentry_role,
         m.id,
         m.group_created,
         m.role_created,
         m.group_associated,
         mg.id,
         mg.location_access,
         mg.database_access,

         h.name,
         h.location,
         r.distinguished_name,
         r.common_name,
         r.sentry_role,
         r.id,
         r.group_created,
         r.role_created,
         r.group_associated,
         rg.id,
         rg.location_access,
         rg.database_access,

         h.id,
         h.directory_created
       from hive_database h
       inner join hive_grant mg on h.manager_group_id = mg.id
       inner join ldap_registration m on mg.ldap_registration_id = m.id
       left join hive_grant rg on h.readonly_group_id = rg.id
       left join ldap_registration r on rg.ldap_registration_id = r.id
      """

    def insert(hiveDatabase: HiveDatabase): Update0 =
      sql"""
         insert into hive_database (name, location, size_in_gb, manager_group_id, readonly_group_id)
         values (${hiveDatabase.name}, ${hiveDatabase.location}, ${hiveDatabase.sizeInGB}, ${hiveDatabase.managingGroup.id}, ${hiveDatabase.readonlyGroup.flatMap(_.id)})
         """.update

    def find(id: Long): Query0[HiveDatabase] =
      (selectQuery ++ whereAnd(fr"h.id = $id")).query[HiveDatabase]

    def list(workspaceId: Long): Query0[HiveDatabase] =
      (selectQuery ++ fr"inner join workspace_database wd on wd.hive_database_id = h.id" ++ whereAnd(fr"wd.workspace_request_id = $workspaceId")).query[HiveDatabase]

    def directoryCreated(id: Long): Update0 =
      sql"""
          update hive_database
          set directory_created = ${Instant.now(clock)}
          where id = $id
          """.update

    def quotaSet(id: Long): Update0 =
      sql"""
          update hive_database
          set quota_set = ${Instant.now(clock)}
          where id = $id
          """.update

    def databaseCreated(id: Long): Update0 =
      sql"""
          update hive_database
          set database_created = ${Instant.now(clock)}
          where id = $id
          """.update

  }

}
