package io.phdata.services

import java.util.concurrent.Executors

import cats.effect.concurrent.MVar
import cats.effect.{ContextShift, IO, Timer}
import io.phdata.caching.{CacheEntry, Cached}
import io.phdata.clients.CMClient
import io.phdata.config.ServiceOverride
import io.phdata.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with MockFactory {

  behavior of "CDH Cluster service"

  it should "use hue override" in new Context {
    val newConfig = appConfig.cluster.copy(hueOverride = ServiceOverride(Some("abc"), 8088))
    val timedCacheService = new TimedCacheService()
    val clusterCache: Cached[IO, Seq[Cluster]] = MVar[IO].of(CacheEntry(1000, Seq.empty[Cluster])).unsafeRunSync

    val service = new CDHClusterService(httpClient, newConfig, timedCacheService, clusterCache)

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

    val httpClient = new CMClient[IO](testClient, appConfig.cluster)
  }
}
