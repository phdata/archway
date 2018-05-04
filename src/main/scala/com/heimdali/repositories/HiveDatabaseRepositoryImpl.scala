package com.heimdali.repositories

import com.heimdali.models.HiveDatabase
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class HiveDatabaseRepositoryImpl(implicit executionContext: ExecutionContext)
  extends HiveDatabaseRepository {

  override def create(hiveDatabase: HiveDatabase): Future[HiveDatabase] = Future {
    NamedDB('default) autoCommit { implicit session =>
      val id = applyUpdateAndReturnGeneratedKey {
        val m = HiveDatabase.column
        insert.into(HiveDatabase)
          .namedValues(
            m.name -> hiveDatabase.name,
            m.role -> hiveDatabase.role,
            m.location -> hiveDatabase.location,
            m.sizeInGB -> hiveDatabase.sizeInGB
          )
      }

      hiveDatabase.copy(id = Some(id))
    }
  }

}
