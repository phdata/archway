package com.heimdali.repositories

import com.heimdali.models.HiveDatabase
import com.heimdali.test.fixtures._
import org.scalatest.{AsyncFlatSpec, Matchers}

class HiveDatabaseRepositorySpec extends AsyncFlatSpec with Matchers with DBTest {

  behavior of "Hive Database Repository"

  it should "Save and extract a record just fine" in {
    val repository = new HiveDatabaseRepositoryImpl()
    repository.create(hive).map { newRecord =>
      newRecord.id shouldBe defined
    }
  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(HiveDatabase)
}