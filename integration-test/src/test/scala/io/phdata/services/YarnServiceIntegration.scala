package io.phdata.services

import cats.effect.{ContextShift, IO, Resource}
import cats.implicits._
import io.phdata.AppContext
import io.phdata.caching.CacheEntry
import io.phdata.models.Yarn
import io.phdata.test.fixtures._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class YarnServiceIntegration extends FlatSpec with Matchers {
  System.setProperty("javax.net.ssl.trustStore", "itest-config/valhalla.jks")

  it should "create resource pool, and update that resource pool" in new Context {
    val workspaceId: Long = 246
    val updateYarn = new Yarn("root.governed_test_1_6_2", 100, 100)
    val result = resources.use { service =>
      for {
        currentYarn <- service.list(workspaceId)
        _ <- service.updateYarnResources(updateYarn, currentYarn.get(0).get.id.get, timer.instant)
        dbGroups <- service.list(workspaceId)
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.get(0).get.poolName == updateYarn.poolName)
    assert(result.get(0).get.maxCores == updateYarn.maxCores)
    assert(result.get(0).get.maxMemoryInGB == updateYarn.maxMemoryInGB)
  }

  trait Context{
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer
    val resources = for {
      ctx <- AppContext.default[IO]()
      _ <- Resource.liftF(ctx.clusterCache.put(CacheEntry(0L, Seq.empty)))
    } yield new YarnServiceImpl[IO](ctx)
  }
}
