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
      update yarn
      set completed = ${Instant.now()}
      where id = $id
      """.update.run

  def insert(yarn: Yarn): ConnectionIO[Long] =
    sql"""
       insert into yarn (pool_name, max_cores, max_memory_in_gb)
       values(
        ${yarn.poolName},
        ${yarn.maxCores},
        ${yarn.maxMemoryInGB}
       )
      """.update.withUniqueGeneratedKeys[Long]("id")

  val selectQuery =
    sql"""
       select
         y.pool_name,
         y.max_cores,
         y.max_memory_in_gb,
         y.id
       from
         yarn y
      """

  def find(id: Long): OptionT[ConnectionIO, Yarn] =
    OptionT {
      (selectQuery ++ whereAnd(fr"y.id = $id")).query[Yarn].option
    }

  override def create(yarn: Yarn): ConnectionIO[Yarn] =
    for {
      id <- insert(yarn)
      result <- find(id).value
    } yield result.get

  override def findByWorkspace(id: Long): ConnectionIO[List[Yarn]] =
      (selectQuery ++
        fr"inner join request_yarn ry on ry.yarn_id = y.id" ++
        fr"inner join workspace_request w on ry.workspace_request_id = w.id" ++
        whereAnd(fr"w.id = $id")).query[Yarn].to[List]

}
