package com.heimdali.repositories

import com.heimdali.common.IntegrationTest
import com.heimdali.test.fixtures._
import org.scalatest.{FlatSpec, Matchers}

class ComplianceRepositoryImplIntegrationSpec extends FlatSpec with Matchers with DBTest with IntegrationTest {

  behavior of "ComplianceRepositoryImplIntegrationSpec"

  it should "create" in {

    val complianceRepository = new ComplianceRepositoryImpl

    complianceRepository.create(savedCompliance).map { newRecord =>
      newRecord.id shouldBe defined
    }

  }
}
