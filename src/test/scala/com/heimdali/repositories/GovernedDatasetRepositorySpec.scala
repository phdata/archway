package com.heimdali.repositories

import com.heimdali.models.{Compliance, Dataset, GovernedDataset}
import com.heimdali.test.fixtures.DBTest
import org.joda.time.DateTime
import org.scalatest._
import scalikejdbc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GovernedDatasetRepositorySpec extends FlatSpec with Matchers with DBTest with BeforeAndAfterAll {
  var newComplianceId: Long = _
  var newRawId: Long = _
  var newStagingId: Long = _
  var newModeledId: Long = _

  import scala.concurrent.ExecutionContext.Implicits.global

  behavior of "GovernedDatasetRepositorySpec"

  it should "create" in {
    val repo = new GovernedDatasetRepositoryImpl()
    Await.result(
      repo.create(GovernedDataset(
        None,
        "name",
        "system",
        "purpose",
        new DateTime(),
        Some("username"),
        1,
        1,
        1,
        complianceId = Some(newComplianceId),
        rawDatasetId = Some(newRawId),
        stagingDatasetId = Some(newStagingId),
        modeledDatasetId = Some(newModeledId))),
      Duration.Inf)
    val result = Await.result(repo.find(Seq("system")), Duration.Inf)
    result.size should be(1)
  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq.empty

  override protected def beforeAll(): Unit = {
    NamedDB('default) localTx { implicit session =>
      newComplianceId = applyUpdateAndReturnGeneratedKey(insert.into(Compliance).values(null, true, true, true))
      newRawId = applyUpdateAndReturnGeneratedKey(insert.into(Dataset).values(null, "name", "system", "purpose", null, null, null))
      newStagingId = applyUpdateAndReturnGeneratedKey(insert.into(Dataset).values(null, "name", "system", "purpose", null, null, null))
      newModeledId = applyUpdateAndReturnGeneratedKey(insert.into(Dataset).values(null, "name", "system", "purpose", null, null, null))
    }
  }

  override protected def afterAll(): Unit = {
    NamedDB('default) localTx { implicit session =>
      applyUpdate(delete.from(GovernedDataset))
      applyUpdate(delete.from(Compliance))
      applyUpdate(delete.from(Dataset))
    }
  }
}

