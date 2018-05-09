package com.heimdali.models

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers, PropSpec}
import org.scalatest.prop.TableDrivenPropertyChecks

class DatasetSpec extends FlatSpec with TableDrivenPropertyChecks with Matchers {

  val tests =
    Table(
      ("givenSystemName", "givenEnvironment", "expectedDatabase", "expectedGroupName", "expectedRole", "expectedDataDirectory"),
      ("sesame", "dev", "raw_sesame", "edh_dev_raw_sesame", "role_dev_raw_sesame", "/data/governed/raw/sesame")
    )

  def config(environment: String) =
    ConfigFactory.parseString(
      s"""
        | hdfs.datasetRoot = /data/governed
        | cluster.environment = $environment
      """.stripMargin)

  "A dataset" should "provide valid values for workspaces" in {
    forAll(tests) { (givenSystemName: String, givenEnvironment: String, expectedDatabse: String, expectedGroupName: String, expectedRole: String, expectedDataDirectory: String) =>
      val dataset = Dataset(Dataset.RawDataset, "sesame", "username")
      val conf = config(givenEnvironment)
      dataset.databaseName should be(expectedDatabse)
      dataset.groupName(conf) should be(expectedGroupName)
      dataset.dataDirectory(conf) should be(expectedDataDirectory)
      dataset.role(conf) should be(expectedRole)
    }
  }

}
