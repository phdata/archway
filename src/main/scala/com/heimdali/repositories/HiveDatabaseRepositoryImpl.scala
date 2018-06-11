package com.heimdali.repositories

import java.time.Instant

import cats.data.OptionT
import com.heimdali.models.HiveDatabase
import doobie._
import doobie.implicits._

class HiveDatabaseRepositoryImpl
  extends HiveDatabaseRepository {

  override def complete(id: Long): ConnectionIO[Int] =
    sql"""
      update hive_database
      set completed = ${Instant.now()}
      where id = $id
      """.update.run

  def insert(hiveDatabase: HiveDatabase): ConnectionIO[Long] =
    sql"""
       insert into hive_database (name, location, size_in_gb, managing_group_id, read_only_group_id)
       values(
        ${hiveDatabase.name},
        ${hiveDatabase.location},
        ${hiveDatabase.sizeInGB},
        ${hiveDatabase.managingGroup.id},
        ${hiveDatabase.readonlyGroup.flatMap(_.id)}
       )
      """.updateWithLogHandler(LogHandler.jdkLogHandler).withUniqueGeneratedKeys[Long]("id")

  def find(id: Long): OptionT[ConnectionIO, HiveDatabase] =
    OptionT {
      sql"""
       select
         h.name,
         h.location,
         h.size_in_gb,
         m.distinguished_name,
         m.common_name,
         m.sentry_role,
         m.id,
         r.distinguished_name,
         r.common_name,
         r.sentry_role,
         r.id,
         h.id
       from hive_database h
       inner join ldap_registration m on h.managing_group_id = m.id
       left join ldap_registration r on h.read_only_group_id = r.id
       where
         h.id = $id
      """.query[HiveDatabase].option
    }

  override def create(hiveDatabase: HiveDatabase): ConnectionIO[HiveDatabase] =
    for {
      id <- insert(hiveDatabase)
      hive <- find(id).value
    } yield hive.get
}
