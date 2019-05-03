package com.heimdali.services

import java.util.concurrent.TimeUnit

import cats.effect.concurrent.MVar
import cats.effect.{Clock, ConcurrentEffect}
import cats.implicits._

import scala.concurrent.duration.Duration

class TimedCacheService[F[_]: ConcurrentEffect, A](cacheDuration: Duration)(implicit clock: Clock[F])
    extends CacheService[F, A] {

  val cache: F[MVar[F, (Long, A)]] = MVar[F].empty[(Long, A)]

  override def initialize(work: F[A]): F[Unit] =
    for {
      cacheStore <- cache
      readyToCache <- work
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      _ <- cacheStore.put(time, readyToCache)
    } yield ()

  def cacheIsValid(cachedMillis: Long): F[Boolean] =
    Clock[F].realTime(TimeUnit.MILLISECONDS).map { time =>
      time - cacheDuration.toMillis > cachedMillis
    }

  def fetch(work: F[A]): F[(Long, A)] = {
    for {
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      readyToCache <- work
    } yield (time, readyToCache)
  }

  override def getOrRun(work: F[A]): F[A] =
    for {
      cacheStore <- cache
      existingCache <- cacheStore.take
      valid <- cacheIsValid(existingCache._1)
      newValue <- if (valid) (existingCache._1, existingCache._2).pure[F] else fetch(work)
      _ <- cacheStore.put(newValue)
    } yield newValue._2

}
