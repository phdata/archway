package com.heimdali.repositories

import com.heimdali.models.SharedWorkspace
import com.heimdali.test.fixtures._
import org.scalatest.{AsyncFlatSpec, Matchers}
import scalikejdbc.ConnectionPool

class SharedWorkspaceRepositorySpec extends AsyncFlatSpec with Matchers with DBTest {

  behavior of "Account Repository"

  it should "Save and extract a record just fine" in {
    val repository = new SharedWorkspaceRepositoryImpl()
    repository.create(initialSharedWorkspace).map { newRecord =>
      newRecord.id shouldBe defined
    }
  }
  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(SharedWorkspace)
}
