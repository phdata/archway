package com.heimdali.repositories

import com.heimdali.models.Yarn
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class YarnRepositoryImpl(implicit executionContext: ExecutionContext)
  extends YarnRepository
    with LazyLogging {
  override def create(yarn: Yarn): Future[Yarn] = Future {
    logger.info("creating yarn record {}")
    NamedDB('default) localTx { implicit session =>
      val id = applyUpdateAndReturnGeneratedKey {
        val y = Yarn.column
        insert.into(Yarn)
          .namedValues(
            y.poolName -> yarn.poolName,
            y.maxCores -> yarn.maxCores,
            y.maxMemoryInGB -> yarn.maxMemoryInGB
          )
      }
      yarn.copy(id = Some(id))
    }
  }
}
