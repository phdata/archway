package com.heimdali.repositories

import com.heimdali.models.Compliance
import com.heimdali.test.fixtures._
import org.scalatest.{AsyncFlatSpec, Matchers}
import scalikejdbc.ConnectionPool

class ComplianceRepositoryImplSpec extends AsyncFlatSpec with Matchers with DBTest {

  behavior of "ComplianceRepositoryImplSpec"

  it should "create" in {

    val complianceRepository = new ComplianceRepositoryImpl

    complianceRepository.create(compliance).map { newRecord =>
      newRecord.id shouldBe defined
    }

  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(Compliance)
}
