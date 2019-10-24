package io.phdata.services

import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.itest.fixtures.SSLTest
import io.phdata.models.Yarn
import io.phdata.test.fixtures._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class YarnServiceIntegration extends FlatSpec with Matchers with SSLTest {

  it should "create resource pool, and update that resource pool" in new Context {
    val workspaceId: Long = 246
    val updateYarn = new Yarn("root.governed_test_1_6_2", 100, 100)
    val result = resources.use { service =>
      for {
        currentYarn <- service.list(workspaceId)
        _ <- service.updateYarnResources(updateYarn, currentYarn.head.id.get, timer.instant)
        dbGroups <- service.list(workspaceId)
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.head.poolName == updateYarn.poolName)
    assert(result.head.maxCores == updateYarn.maxCores)
    assert(result.head.maxMemoryInGB == updateYarn.maxMemoryInGB)
  }

  trait Context{
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer
    val resources = for {
      ctx <- AppContext.default[IO]()
    } yield new YarnServiceImpl[IO](ctx)
  }
}
