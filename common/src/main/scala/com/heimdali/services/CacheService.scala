package com.heimdali.services

import cats.effect.{Clock, Concurrent}
import com.heimdali.caching.{CacheEntry, Cached}

import scala.concurrent.duration.FiniteDuration

trait CacheService {

  def initial[F[_]: Concurrent, A]: F[Cached[F, A]]

  def getOrRun[F[_]: Concurrent: Clock, A](expiration: FiniteDuration, work: F[A], cache: Cached[F, A]): F[A]
}
