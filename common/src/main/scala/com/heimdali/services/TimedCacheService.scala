package com.heimdali.services

import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.concurrent.MVar
import cats.effect.{Clock, Concurrent}
import cats.implicits._
import com.heimdali.caching._

import scala.concurrent.duration.FiniteDuration

class TimedCacheService
  extends CacheService{

  override def initial[F[_] : Concurrent, A]: F[Cached[F, A]] =
    MVar[F].empty[CacheEntry[A]]

  def cacheIsValid[F[_] : Monad : Clock](expiration: FiniteDuration, cachedMillis: Long): F[Boolean] =
    Clock[F].realTime(TimeUnit.MILLISECONDS).map { time =>
      time - expiration.toMillis > cachedMillis
    }

  override def run[F[_] : Concurrent : Clock, A](work: F[A]): F[CacheEntry[A]] =
    for {
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      readyToCache <- work
    } yield (time, readyToCache)

  override def getOrRun[F[_] : Concurrent : Clock, A](expiration: FiniteDuration, work: F[A], cache: Cached[F, A]): F[A] =
    for {
      existingCache <- cache.take
      valid <- cacheIsValid[F](expiration, existingCache._1)
      newValue <- if (valid) (existingCache._1, existingCache._2).pure[F] else run(work)
      _ <- cache.put(newValue)
    } yield newValue._2

}
