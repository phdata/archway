package com.heimdali.services

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActor, TestKit}
import com.heimdali.clients.LDAPClient
import com.heimdali.models.{Dataset, GovernedDataset}
import com.heimdali.provisioning.WorkspaceProvisioner.Start
import com.heimdali.repositories._
import com.heimdali.test.fixtures._
import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import scalikejdbc.ConnectionPool

import scala.concurrent.Await
import scala.concurrent.duration._

class GovernedDatasetServiceIntegrationSpec extends FlatSpec with Matchers with MockFactory {

  Class.forName("org.postgresql.Driver")
  ConnectionPool.add('default, "jdbc:postgresql://localhost/heimdali", "postgres", "postgres")

  import scala.concurrent.ExecutionContext.Implicits.global

  behavior of "Governed dataset service"

  it should "create" in new TestKit(ActorSystem()) {
    val ldapClient = mock[LDAPClient]

    val governedDatasetRepository = new GovernedDatasetRepositoryImpl()
    val datasetRepository = new DatasetRepositoryImpl()
    val complianceRepository = new ComplianceRepositoryImpl()
    val factory = mockFunction[Dataset, ActorRef]
    factory expects * returning testActor
    factory expects * returning testActor
    factory expects * returning testActor

    setAutoPilot((sender: ActorRef, msg: Any) => msg match {
      case Start =>
        sender.tell(Start, testActor)
        TestActor.KeepRunning
      case x =>
        println(x)
        TestActor.KeepRunning
    })

    val dataset = GovernedDataset(None,
      name,
      systemName,
      purpose,
      new DateTime(),
      Some(standardUsername),
      hdfsRequestedSize,
      maxCores,
      maxMemoryInGB,
      None,
      Some(compliance))

    val governedDatasetService = new GovernedDatasetServiceImpl(governedDatasetRepository, datasetRepository, complianceRepository, environment, ldapClient, factory)
    val newRecord = Await.result(governedDatasetService.create(dataset), Duration.Inf)
    newRecord.id shouldBe defined
  }

  it should "filter" in {
    val ldapClient = mock[LDAPClient]
    val governedDatasetRepository = mock[GovernedDatasetRepository]
    val datasetRepository = mock[DatasetRepository]
    val complianceRepository = mock[ComplianceRepository]
    val factory = mockFunction[Dataset, ActorRef]
    val governedDatasetService = new GovernedDatasetServiceImpl(governedDatasetRepository, datasetRepository, complianceRepository, environment, ldapClient, factory)

    governedDatasetService.filter(Seq(
      "CN=edm_abc_row_abc,OU=groups,ou=example,ou=com",
      "CN=edh_dev_raw_tony,OU=groups,ou=example,ou=com",
      "CN=edh_dev_raw_home_mortgage,OU=groups,ou=example,ou=com"
    )) should be (Seq(
      "tony",
      "home_mortgage"
    ))
  }

  //TODO: Add find test
  ignore should "find" in {

  }

}
