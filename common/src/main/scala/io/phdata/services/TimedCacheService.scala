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

  def run[F[_]: Concurrent: Clock, A](work: F[A]): F[(Long, Either[Throwable, A])] =
    for {
      time <- Clock[F].realTime(TimeUnit.MILLISECONDS)
      readyToCache <- work.attempt
    } yield (time, readyToCache)

  override def getOrRun[F[_]: Concurrent: Clock, A](
      cacheDuration: FiniteDuration,
      work: F[A],
      cache: Cached[F, A]
  ): F[A] =
    for {
      maybeCache <- cache.tryTake
      existingCache <- maybeCache.getOrElse(throw new Exception("Cache is not initialized")).pure[F]
      valid <- cacheIsValid[F](cacheDuration, existingCache.cachedTime)
      _ <- logger.trace(s"Cache validity: $valid").pure[F]
      newCache <- if (valid) {
        CacheEntry(existingCache.cachedTime, existingCache.value).pure[F]
      } else {
        logger.debug(s"Starting cache refresh")

        val result = run(work).flatMap {
          case (time, Right(value)) => CacheEntry(time, value).pure[F]
          case (time, Left(e)) =>
            logger.error(s"Work failed $work, updating cache with previous value", e).pure[F]
            CacheEntry(time, existingCache.value).pure[F]
        }
        logger.debug(s"Finished cache refresh")
        result
      }
      _ <- cache.put(newCache).onError {
        case e: Throwable =>
          logger.error(s"Cache refresh failed!", e).pure[F]
      }
    } yield newCache.value

}
