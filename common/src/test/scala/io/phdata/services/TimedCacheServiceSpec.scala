package io.phdata.services

import java.util.concurrent.{Executors, TimeUnit}

import cats.effect.concurrent.MVar
import cats.effect.{IO, _}
import cats.implicits._
import io.phdata.caching.CacheEntry
import org.scalatest.{FlatSpec, Matchers}
import io.phdata.test.fixtures._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TimedCacheServiceSpec extends FlatSpec with Matchers {

  behavior of "TimedCache Service"

  it should "initialize empty cache" in new Context {
    val cache = service.initial[IO, Int].unsafeRunSync
    val cacheValue = cache.tryTake.unsafeRunSync

    cacheValue should be(None)
  }

  it should "calculate new cache value" in {
    implicit val timer: Timer[IO] = testTimer
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    val service = new TimedCacheService
    val newCacheValue = service.run[IO, Int](1.pure[IO]).unsafeRunSync

    newCacheValue.cachedTime should be(testTimer.instant.toEpochMilli)
    newCacheValue.value should be(1)
  }

  it should "return cached value when cache is valid" in new Context {
    val actual =
      (for {
        time <- timer.clock.realTime(TimeUnit.MILLISECONDS)
        cachedValue <- MVar.of[IO, CacheEntry[Int]](CacheEntry(time, 1))
        _ <- timer.sleep(200 millis)
        result <- service.getOrRun[IO, Int](500 millis, 2.pure[IO], cachedValue)
      } yield result).unsafeRunSync

    actual should be(1)
  }

  it should "return new value when cache is invalid" in new Context {
    val actual =
      (for {
        time <- timer.clock.realTime(TimeUnit.MILLISECONDS)
        cachedValue <- MVar.of[IO, CacheEntry[Int]](CacheEntry(time, 1))
        _ <- timer.sleep(200 millis)
        result <- service.getOrRun[IO, Int](50 millis, 2.pure[IO], cachedValue)
      } yield result).unsafeRunSync

    actual should be(2)
  }

  trait Context {
    val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    implicit val timer: Timer[IO] = IO.timer(executor)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(executor)

    val service = new TimedCacheService
  }

}
