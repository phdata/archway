package com.heimdali.repositories

import java.time.{Clock, Instant}

import cats.data.OptionT
import com.heimdali.models.Yarn
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class YarnRepositoryImpl extends YarnRepository {

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  override def complete(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .complete(id, time)
      .run

  override def find(id: Long): OptionT[ConnectionIO, Yarn] =
    OptionT {
      Statements
        .find(id)
        .option
    }

  override def create(yarn: Yarn): ConnectionIO[Long] =
    Statements
      .insert(yarn)
      .withUniqueGeneratedKeys[Long]("id")

  override def findByWorkspaceId(id: Long): ConnectionIO[List[Yarn]] =
      Statements
        .findByWorkspace(id)
        .to[List]

  object Statements {

    val selectQuery =
      sql"""
       select
         rp.pool_name,
         rp.max_cores,
         rp.max_memory_in_gb,
         rp.id
       from
         resource_pool rp
      """

    def insert(yarn: Yarn): Update0 =
    sql"""
       insert into resource_pool (pool_name, max_cores, max_memory_in_gb)
       values(
        ${yarn.poolName},
        ${yarn.maxCores},
        ${yarn.maxMemoryInGB}
       )
      """.update

    def complete(id: Long, time: Instant): Update0 =
      sql"""
         update resource_pool
         set created = $time
         where id = $id
         """.update

    def find(id: Long): Query0[Yarn] =
      (selectQuery ++ whereAnd(fr"rp.id = $id")).query[Yarn]

    def findByWorkspace(id: Long): Query0[Yarn] =
      (selectQuery ++ fr"inner join workspace_pool wp on wp.resource_pool_id = rp.id" ++ whereAnd(fr"wp.workspace_request_id= $id")).query[Yarn]

  }

}
