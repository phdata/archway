package com.heimdali.services

import java.util.concurrent.Executors

import cats.effect.concurrent.MVar
import cats.effect.{ContextShift, IO, Timer}
import com.heimdali.caching.{CacheEntry, Cached}
import com.heimdali.clients.HttpClient
import com.heimdali.config.ServiceOverride
import com.heimdali.test.fixtures.{HttpTest, _}
import org.apache.hadoop.conf.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with MockFactory {

  behavior of "CDH Cluster service"

  it should "use hue override" in new Context {
    val httpClient = mock[HttpClient[IO]]
    val configuration = new Configuration()
    val newConfig = appConfig.cluster.copy(hueOverride = ServiceOverride(Some("abc"), 8088))
    val timedCacheService = new TimedCacheService()
    val clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].of(CacheEntry(1000, Seq.empty[Cluster])).unsafeRunSync

    val service = new CDHClusterService(httpClient, newConfig, configuration, timedCacheService, clusterCache)

    val ClusterApp(_, _, _, _, actual) = service.hueApp(null, null, null)
    actual.head._2.head shouldBe AppLocation("abc", 8088)
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
  }
}
