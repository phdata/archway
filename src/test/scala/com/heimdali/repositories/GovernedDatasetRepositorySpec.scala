package com.heimdali.repositories

import com.heimdali.models.{Compliance, GovernedDataset}
import com.heimdali.test.fixtures.DBTest
import org.joda.time.DateTime
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GovernedDatasetRepositorySpec extends FlatSpec with Matchers with DBTest {

  import scala.concurrent.ExecutionContext.Implicits.global

  behavior of "GovernedDatasetRepositorySpec"

  ignore should "find" in {
    val repo = new GovernedDatasetRepositoryImpl()
    Await.result(repo.create(GovernedDataset(None, "name", "system", "purpose", new DateTime(), Some("username"), 1, 1, 1, complianceId = Some(123), rawDatasetId = Some(123), stagingDatasetId = Some(123), modeledDatasetId = Some(123))), Duration.Inf)
    val result = Await.result(repo.find(Seq("system")), Duration.Inf)
    result.size should be(1)
  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(GovernedDataset)
}

