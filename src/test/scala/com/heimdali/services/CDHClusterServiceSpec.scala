package com.heimdali.services

import cats.effect.IO
import com.heimdali.clients.{CMClient, HttpTest}
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "CDH Cluster service"

  it should "return a cluster" in {
    val url = ""
    val name = "cluster name"
    val version = "5.11.2"

    val username = "admin"
    val password = "admin"

    val testClient = IO.pure(Client.fromHttpService(HttpService[IO] {
      case GET -> Root / "clusters" / "cluster name" =>
        Ok(fromResource("cloudera/clusters.cluster_name.actual.json"))

      case GET -> Root / "clusters" / "cluster name" / "services" / "impala" / "roles" =>
        Ok(fromResource("cloudera/impala.json"))

      case GET -> Root / "hosts" / "9e29e533-10f5-4f87-b40a-0f2c58f8fdb1" =>
        Ok(fromResource("cloudera/hosts.json"))

      case GET -> Root / "clusters" / "cluster name" / "services" / "hive" / "roles" =>
        Ok(fromResource("cloudera/hive.json"))

      case GET -> Root / "hosts" / "add85153-1f7c-46af-80a0-facf32966e23" =>
        Ok(fromResource("cloudera/hosts.json"))

      case GET -> Root / "clusters" / "cluster name" / "services" =>
        Ok(fromResource("cloudera/services.json"))
    }))
    val httpClient = new CMClient[IO](testClient, clusterConfig)

    val service = new CDHClusterService(httpClient, clusterConfig)
    val list = service.list.unsafeRunSync()
    list should have length 1
    list.head.id should be(name)
    list.head.name should be("Valhalla")
    list.head.clusterApps should be(Map(
      "ZOOKEEPER" -> BasicClusterApp("zookeeper", "ZooKeeper", "GOOD_HEALTH", "STARTED"),
      "OOZIE" -> BasicClusterApp("oozie", "Oozie", "GOOD_HEALTH", "STARTED"),
      "KS_INDEXER" -> BasicClusterApp("ks_indexer", "Key-Value Store Indexer", "GOOD_HEALTH", "STARTED"),
      "IMPALA" -> HostClusterApp("impala", "Impala", "GOOD_HEALTH", "STARTED", "worker1.valhalla.phdata.io"),
      "HIVESERVER2" -> HostClusterApp("hive", "Hive", "GOOD_HEALTH", "STARTED", "worker1.valhalla.phdata.io")
    ))
    list.head.distribution should be(CDH(version))
    list.head.status should be("GOOD_HEALTH")
  }
}
