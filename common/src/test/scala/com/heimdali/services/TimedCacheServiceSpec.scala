package com.heimdali.services

import java.util.concurrent.Executors

import cats.effect.concurrent.MVar
import cats.effect.{IO, _}
import cats.implicits._
import com.heimdali.caching.CacheEntry
import com.heimdali.test.fixtures._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TimedCacheServiceSpec extends FlatSpec with Matchers{

  behavior of "TimedCache Service"

  it should "initialize empty cache" in new Context {
    val cache = service.initial[IO, Int].unsafeRunSync
    val cacheValue = cache.tryTake.unsafeRunSync

    cacheValue should be (None)
  }

  it should "calculate new cache value" in {
    implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    val service = new TimedCacheService
    val newCacheValue = service.run[IO, Int](1.pure[IO]).unsafeRunSync

    newCacheValue.cachedTime should be (timer.instant.toEpochMilli)
    newCacheValue.entry should be (1)
  }

  it should "return cached value when cache is valid" in new Context {
    val cachedValue = MVar.of[IO, CacheEntry[Int]](CacheEntry(1000, 1)).unsafeRunSync
    val actual = service.getOrRun[IO, Int](System.currentTimeMillis - 1000 milli, 2.pure[IO], cachedValue).unsafeRunSync

    actual should be (1)
  }

  it should "return new value when cache is invalid" in new Context {
    val cachedValue = MVar.of[IO, CacheEntry[Int]](CacheEntry(1000, 1)).unsafeRunSync
    val actual = service.getOrRun[IO, Int](System.currentTimeMillis milli, 2.pure[IO], cachedValue).unsafeRunSync

    actual should be (2)
  }

  trait Context {
    val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    implicit val timer: Timer[IO] = IO.timer(executor)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(executor)

    val service = new TimedCacheService
  }
}
