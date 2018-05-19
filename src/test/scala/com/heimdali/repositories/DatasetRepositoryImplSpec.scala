package com.heimdali.repositories

import com.heimdali.models.Dataset
import com.heimdali.test.fixtures.DBTest
import org.scalatest.{AsyncFlatSpec, Matchers}

class DatasetRepositoryImplSpec
  extends AsyncFlatSpec
    with Matchers
    with DBTest {

  behavior of "ComplianceRepositoryImplSpec"

  it should "create" in {

    val datasetRepository = new DatasetRepositoryImpl

    datasetRepository.create(Dataset(None, "dataset", "dataset", "purpose")).map { newRecord =>
      newRecord.id shouldBe defined
    }

  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(Dataset)
}
