package io.phdata.services

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Timer}
import cats.effect.concurrent.MVar
import io.phdata.caching.{CacheEntry, Cached}
import io.phdata.clients.CMClient
import org.apache.hadoop.conf.Configuration
import org.scalatest.{FlatSpec, Matchers}
import org.http4s.client.blaze.BlazeClientBuilder
import io.phdata.itest.fixtures._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class CDHClusterServiceIntegrationSpec extends FlatSpec with Matchers with SSLTest {

  it should "return a cluster" in new Context {

    val name = "cluster"
    val version = "6.1.1"

    val configuration = new Configuration()
    configuration.set("hive.server2.thrift.port", "888")

    val timedCacheService = new TimedCacheService()
    val clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].of(CacheEntry(1000, Seq.empty[Cluster])).unsafeRunSync

    val service = new CDHClusterService(httpClient, itestConfig.cluster, configuration, timedCacheService, clusterCache)
    val details = service.clusterDetails.unsafeRunSync.head

    val impala = details.services.find(_.name == "impala").get
    impala.capabilities("beeswax").head.port shouldBe 21000
    impala.capabilities("hiveServer2").head.port shouldBe 21050

    val hive = details.services.find(_.name == "hive").get
    hive.capabilities("thrift").head.port shouldBe 888

    val hue = details.services.find(_.name == "hue").get
    hue.capabilities("load_balancer").head.port shouldBe 8088

    val yarn = details.services.find(_.name == "yarn").get
    yarn.capabilities("node_manager").head.port shouldBe 8044
    yarn.capabilities("resource_manager").head.port shouldBe 8090

    val mgmt = details.services.find(_.name == "mgmt").get
    mgmt.capabilities("navigator").head.port shouldBe 7187

    details.id should be(name)
    details.name should be("Valhalla")
    details.distribution should be(CDH(version))
    details.status should be("GOOD_HEALTH")
  }

  trait Context {
    val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    implicit val timer: Timer[IO] = IO.timer(executor)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(executor)
    val h4Client = BlazeClientBuilder[IO](executor)
      .withRequestTimeout(5 minutes)
      .withResponseHeaderTimeout(5 minutes)
      .resource
    val httpClient = new CMClient[IO](h4Client, itestConfig.cluster)
  }
}
