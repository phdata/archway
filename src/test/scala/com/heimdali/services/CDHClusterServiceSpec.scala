package com.heimdali.services

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.heimdali.clients.HttpClient
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.io.Source

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with FailFastCirceSupport
    with MockFactory {

  behavior of "CDH Cluster service"

  it should "return a cluster" in {
    val url = ""
    val name = "cluster name"
    val version = "5.11.2"

    val username = "admin"
    val password = "admin"

    val configuration = ConfigFactory.defaultApplication()
    val Right(clusterJson) = io.circe.parser.parse(Source.fromResource("cloudera/cluster.json").getLines().mkString)
    val Right(impalaJson) = io.circe.parser.parse(Source.fromResource("cloudera/impala.json").getLines().mkString)
    val Right(hiveJson) = io.circe.parser.parse(Source.fromResource("cloudera/hive.json").getLines().mkString)
    val Right(hostsJson) = io.circe.parser.parse(Source.fromResource("cloudera/hosts.json").getLines().mkString)
    val Right(servicesJson) = io.circe.parser.parse(Source.fromResource("cloudera/services.json").getLines().mkString)

    val http = mock[HttpClient]
    val clusterRequest = Get("/clusters/cluster+name").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(clusterRequest).returning(Marshal(clusterJson).to[HttpResponse])
    val impalaRequest = Get("/clusters/cluster+name/services/impala/roles").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(impalaRequest).returning(Marshal(impalaJson).to[HttpResponse])
    val impalaHostRequest = Get("/hosts/9e29e533-10f5-4f87-b40a-0f2c58f8fdb1").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(impalaHostRequest).returning(Marshal(hostsJson).to[HttpResponse])
    val hiveRequest = Get("/clusters/cluster+name/services/hive/roles").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(hiveRequest).returning(Marshal(hiveJson).to[HttpResponse])
    val hs2HostRequest = Get("/hosts/add85153-1f7c-46af-80a0-facf32966e23").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(hs2HostRequest).returning(Marshal(hostsJson).to[HttpResponse])
    val servicesRequest = Get("/clusters/cluster+name/services").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(servicesRequest).returning(Marshal(servicesJson).to[HttpResponse])

    val service = new CDHClusterService(http, configuration)(ExecutionContext.global, materializer)
    val list = Await.result(service.list, Duration.Inf)
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
