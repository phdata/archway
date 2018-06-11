package com.heimdali.repositories

import java.time.Instant

import cats.data.OptionT
import com.heimdali.models.Yarn
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._

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

  def find(id: Long): OptionT[ConnectionIO, Yarn] =
    OptionT {
      sql"""
       select
         pool_name,
         max_cores,
         max_memory_in_gb,
         id
       from
         yarn
       where
         id = $id
      """.query[Yarn].option
    }

  override def create(yarn: Yarn): ConnectionIO[Yarn] =
    for {
      id <- insert(yarn)
      result <- find(id).value
    } yield result.get
}
