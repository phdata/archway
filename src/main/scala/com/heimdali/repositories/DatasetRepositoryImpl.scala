package com.heimdali.repositories

import com.heimdali.models._
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class DatasetRepositoryImpl(implicit executionContext: ExecutionContext)
  extends DatasetRepository {

  def get(id: Long)(implicit session: DBSession): Dataset = {
    val d = Dataset.syntax
    val l = LDAPRegistration.syntax
    val h = HiveDatabase.syntax
    val y = Yarn.syntax
    withSQL {
      select.from(Dataset as d)
        .leftJoin(LDAPRegistration as l).on(d.ldapRegistrationId, l.id)
        .leftJoin(HiveDatabase as h).on(d.hiveDatabaseId, h.id)
        .leftJoin(Yarn as y).on(d.yarnId, y.id)
        .where.eq(d.id, id)
    }.map(Dataset(d.resultName, l.resultName, h.resultName, y.resultName))
      .single().apply().get
  }

  override def find(id: Long, dataset: String): Future[Option[Dataset]] = Future {
    NamedDB('default) readOnly { implicit session =>
      val gd = GovernedDataset.syntax
      val d = Dataset.syntax
      val l = LDAPRegistration.syntax
      val h = HiveDatabase.syntax
      val y = Yarn.syntax
      withSQL {
        select.from(GovernedDataset as gd)
          .innerJoin(Dataset as d).on(sqls"${gd.column(s"${dataset}_dataset_id")} = ${d.id}")
          .leftJoin(LDAPRegistration as l).on(d.ldapRegistrationId, l.id)
          .leftJoin(HiveDatabase as h).on(d.hiveDatabaseId, h.id)
          .leftJoin(Yarn as y).on(d.yarnId, y.id)
          .where.eq(gd.id, id)
      }.map(Dataset(d.resultName, l.resultName, h.resultName, y.resultName))
        .single().apply()
    }
  }

  override def create(dataset: Dataset): Future[Dataset] = Future {
    NamedDB('default) localTx { implicit session =>
      val id = applyUpdateAndReturnGeneratedKey {
        val d = Dataset.column
        insert.into(Dataset)
          .namedValues(
            d.name -> dataset.name,
            d.systemName -> dataset.systemName,
            d.purpose -> dataset.purpose
          )
      }

      dataset.copy(id = Some(id))
    }
  }

  override def setLDAP(datasetId: String, ldapRegistrationId: Long): Future[Dataset] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(Dataset)
          .set(
            Dataset.column.ldapRegistrationId -> ldapRegistrationId
          )
          .where.eq(Dataset.column.id, datasetId.toLong)
      }
      get(datasetId.toLong)
    }
  }

  override def setHive(datasetId: String, hiveDatbaseId: Long): Future[Dataset] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(Dataset)
          .set(
            Dataset.column.hiveDatabaseId -> hiveDatbaseId
          )
          .where.eq(Dataset.column.id, datasetId.toLong)
      }
      get(datasetId.toLong)
    }
  }
}

