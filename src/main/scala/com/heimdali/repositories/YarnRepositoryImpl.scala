package com.heimdali.repositories

import java.time.Instant

import cats.data.OptionT
import com.heimdali.models.Yarn
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class YarnRepositoryImpl
  extends YarnRepository
    with LazyLogging {

  override def complete(id: Long): ConnectionIO[Int] =
    sql"""
      update resource_pool
      set created = ${Instant.now()}
      where id = $id
      """.update.run

  def insert(yarn: Yarn): ConnectionIO[Long] =
    sql"""
       insert into resource_pool (pool_name, max_cores, max_memory_in_gb, workspace_request_id)
       values(
        ${yarn.poolName},
        ${yarn.maxCores},
        ${yarn.maxMemoryInGB},
        ${yarn.workspaceRequestId}
       )
      """.update.withUniqueGeneratedKeys[Long]("id")

  val selectQuery =
    sql"""
       select
         rp.pool_name,
         rp.max_cores,
         rp.max_memory_in_gb,
         rp.workspace_request_id,
         rp.id
       from
         resource_pool rp
      """

  def find(id: Long): OptionT[ConnectionIO, Yarn] =
    OptionT {
      (selectQuery ++ whereAnd(fr"rp.id = $id")).query[Yarn].option
    }

  override def create(yarn: Yarn): ConnectionIO[Yarn] =
    for {
      id <- insert(yarn)
      result <- find(id).value
    } yield result.get

  override def findByWorkspace(id: Long): ConnectionIO[List[Yarn]] =
      (selectQuery ++ whereAnd(fr"rp.workspace_request_id= $id")).query[Yarn].to[List]

}
