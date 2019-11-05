package io.phdata.repositories

import io.phdata.test.fixtures._
import org.scalatest.{FlatSpec, Matchers}

class ComplianceRepositoryImplIntegrationSpec extends FlatSpec with Matchers with DBTest {

  behavior of "ComplianceRepositoryImplIntegrationSpec"

  it should "create" in {

    val complianceRepository = new ComplianceRepositoryImpl()

    complianceRepository.create(savedCompliance, id).map { newRecordId =>
      newRecordId shouldBe defined
    }
  }
}
