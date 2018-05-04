package com.heimdali.repositories

import com.heimdali.models._
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class ComplianceRepositoryImpl(implicit executionContext: ExecutionContext)
  extends ComplianceRepository {

  override def create(compliance: Compliance): Future[Compliance] = Future {
    NamedDB('default) localTx { implicit session =>
      val id = applyUpdateAndReturnGeneratedKey {
        val c = Compliance.column
        insert.into(Compliance)
            .namedValues(
              c.phiData -> compliance.phiData,
              c.piiData -> compliance.piiData,
              c.pciData -> compliance.pciData
            )
      }
      compliance.copy(id = Some(id))
    }
  }

}
