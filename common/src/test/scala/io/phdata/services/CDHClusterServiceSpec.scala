package io.phdata.services

import java.util.concurrent.Executors

import cats.effect.concurrent.MVar
import cats.effect.{ContextShift, IO, Timer}
import io.phdata.caching.{CacheEntry, Cached}
import io.phdata.clients.CMClient
import io.phdata.config.ServiceOverride
import io.phdata.test.fixtures.{HttpTest, _}
import io.phdata.test.fixtures.HttpTest
import org.apache.hadoop.conf.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "CDH Cluster service"

  it should "use hue override" in new Context {
    val configuration = new Configuration()
    val newConfig = appConfig.cluster.copy(hueOverride = ServiceOverride(Some("abc"), 8088))
    val timedCacheService = new TimedCacheService()
    val clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].of(CacheEntry(1000, Seq.empty[Cluster])).unsafeRunSync

    val service = new CDHClusterService(httpClient, newConfig, configuration, timedCacheService, clusterCache)

    val ClusterApp(_, _, _, _, actual) = service.hueApp(null, null, null)
    actual.head._2.head shouldBe AppLocation("abc", 8088)
  }

  it should "return a cluster" in new Context {
    val url = ""
    val name = "cluster"
    val version = "5.15.0"

    val username = "admin"
    val password = "admin"

    val configuration = new Configuration()
    configuration.set("hive.server2.thrift.port", "888")
    configuration.set("yarn.nodemanager.webapp.address", "0.0.0.0:9998")
    configuration.set("yarn.resourcemanager.webapp.address", "0.0.0.0:9999")

    val timedCacheService = new TimedCacheService()
    val clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].of(CacheEntry(1000, Seq.empty[Cluster])).unsafeRunSync

    val service = new CDHClusterService(httpClient, appConfig.cluster, configuration, timedCacheService, clusterCache)
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
    details.name should be("Odin")
    details.distribution should be(CDH(version))
    details.status should be("GOOD_HEALTH")
  }

//  it should "use the cache service" in new Context {
//    val configuration = new Configuration()
//    val timedCacheService = new TimedCacheService()
//    val clusterCache = MVar[IO].of(CacheEntry(System.currentTimeMillis(), Seq(cluster))).unsafeRunSync
//
//    val service = new CDHClusterService(httpClient, appConfig.cluster, configuration, timedCacheService, clusterCache)
//    val list = service.list.unsafeRunSync
//
//    list should be (Seq(cluster))
//  }

  trait Context {

    val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    implicit val timer: Timer[IO] = IO.timer(executor)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(executor)

    val httpClient = new CMClient[IO](testClient, appConfig.cluster)
  }
}
