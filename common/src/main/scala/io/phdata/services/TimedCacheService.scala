package io.phdata.services

import java.util.concurrent.TimeUnit

import cats.Functor
import cats.effect.concurrent.MVar
import cats.effect.{Clock, Concurrent}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.phdata.caching._

import scala.concurrent.duration.FiniteDuration

class TimedCacheService extends CacheService with LazyLogging {

  override def initial[F[_]: Concurrent, A]: F[Cached[F, A]] =
    MVar[F].empty[CacheEntry[A]]

  def cacheIsValid[F[_]: Functor: Clock](lifetime: FiniteDuration, createdTime: Long): F[Boolean] =
    Clock[F].realTime(TimeUnit.MILLISECONDS).map { currentTime =>
      logger.trace(s"Current time $currentTime, cache lifetime ${lifetime.toMillis}, cache create time $createdTime")
      (currentTime - lifetime.toMillis) < createdTime
    }

  def run[F[_]: Concurrent: Clock, A](work: F[A]): F[CacheEntry[A]] =
    for {
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      readyToCache <- work.onError {
        case e: Throwable => logger.error(s"Work failed $work", e).pure[F]
      }
    } yield CacheEntry(time, readyToCache)

  override def getOrRun[F[_]: Concurrent: Clock, A](
      cacheDuration: FiniteDuration,
      work: F[A],
      cache: Cached[F, A]
  ): F[A] =
    for {
      existingCache <- cache.take
      valid <- cacheIsValid[F](cacheDuration, existingCache.cachedTime)
      _ <- logger.trace(s"Cache validity: $valid").pure[F]
      newCache <- if (valid) {
        CacheEntry(existingCache.cachedTime, existingCache.value).pure[F]
      } else {
        logger.debug(s"Starting cache refresh")
        val result = run(work)
        logger.debug(s"Finished cache refresh")
        result
      }
      _ <- cache.put(newCache).onError {
        case e: Throwable =>
          logger.error(s"Cache refresh failed!", e).pure[F]
      }
    } yield newCache.value

}
