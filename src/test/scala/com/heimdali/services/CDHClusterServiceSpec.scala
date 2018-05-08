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
    val name = "cluster"
    val version = "5.11.2"

    val username = "admin"
    val password = "admin"

    val configuration = ConfigFactory.defaultApplication()
    val Right(clusterJson) = io.circe.parser.parse(Source.fromResource("cloudera/cluster.json").getLines().mkString)
    val Right(impalaJson) = io.circe.parser.parse(Source.fromResource("cloudera/impala.json").getLines().mkString)
    val Right(hostsJson) = io.circe.parser.parse(Source.fromResource("cloudera/hosts.json").getLines().mkString)
    val Right(servicesJson) = io.circe.parser.parse(Source.fromResource("cloudera/services.json").getLines().mkString)

    val http = mock[HttpClient]
    val clusterRequest = Get("/clusters/cluster").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(clusterRequest).returning(Marshal(clusterJson).to[HttpResponse])
    val impalaRequest = Get("/clusters/cluster/services/impala/roles").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(impalaRequest).returning(Marshal(impalaJson).to[HttpResponse])
    val hostsRequest = Get("/hosts/9e29e533-10f5-4f87-b40a-0f2c58f8fdb1").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(hostsRequest).returning(Marshal(hostsJson).to[HttpResponse])
    val servicesRequest = Get("/clusters/cluster/services").addCredentials(BasicHttpCredentials(username, password))
    (http.request _).expects(servicesRequest).returning(Marshal(servicesJson).to[HttpResponse])

    val service = new CDHClusterService(http, configuration)(ExecutionContext.global, materializer)
    val list = Await.result(service.list, Duration.Inf)
    list should have length 1
    list.head.id should be(name)
    list.head.name should be("Valhalla")
    list.head.clusterApps should be(Map(
      "ZOOKEEPER" -> BasicClusterApp("ZooKeeper", "GOOD_HEALTH", "STARTED"),
      "OOZIE" -> BasicClusterApp("Oozie", "GOOD_HEALTH", "STARTED"),
      "KS_INDEXER" -> BasicClusterApp("Key-Value Store Indexer", "GOOD_HEALTH", "STARTED"),
      "IMPALA" -> HostClusterApp("Impala", "GOOD_HEALTH", "STARTED", "worker1.valhalla.phdata.io")
    ))
    list.head.distribution should be(CDH(version))
    list.head.status should be("GOOD_HEALTH")
  }
}