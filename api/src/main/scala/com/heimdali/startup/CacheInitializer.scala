package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.caching.CacheEntry
import com.typesafe.scalalogging.LazyLogging

class CacheInitializer[F[_] : Sync : ContextShift : Timer](context: AppContext[F])
  extends ScheduledJob[F] with LazyLogging {

  override def work: F[Unit] =
    for {
      _ <- logger.info("initializing cache").pure[F]
      _ <- context.clusterCache.put(CacheEntry(0L, Seq.empty))
      _ <- logger.info("initialized cluster detail cache with nothing").pure[F]
    } yield ()

}

