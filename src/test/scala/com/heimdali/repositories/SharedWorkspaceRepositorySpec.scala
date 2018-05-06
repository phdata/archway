package com.heimdali.repositories

import com.heimdali.models.{Compliance, SharedWorkspace}
import com.heimdali.test.fixtures._
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import scalikejdbc._

class SharedWorkspaceRepositorySpec extends AsyncFlatSpec with Matchers with DBTest with BeforeAndAfterAll {
  var newComplianceId: Long = _

  behavior of "Account Repository"

  ignore should "Save and extract a record just fine" in {
    val repository = new SharedWorkspaceRepositoryImpl()
    repository.create(initialSharedWorkspace.copy(complianceId = Some(newComplianceId))).map { newRecord =>
      newRecord.id shouldBe defined
    }
  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(SharedWorkspace)

  override protected def beforeAll(): Unit = {
    NamedDB('default) localTx { implicit session =>
      newComplianceId = applyUpdateAndReturnGeneratedKey {
        insert.into(Compliance)
          .values(null, true, true, true)
      }
    }
  }

  override protected def afterAll(): Unit = {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        delete.from(Compliance)
      }
    }
  }
}
