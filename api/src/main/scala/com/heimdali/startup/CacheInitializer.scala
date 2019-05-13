package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.caching.{CacheEntry, Cached}
import com.heimdali.services.Cluster
import com.typesafe.scalalogging.LazyLogging

class CacheInitializer[F[_] : Sync : ContextShift : Timer](clusterCache: Cached[F, Seq[Cluster]])
  extends ScheduledJob[F] with LazyLogging {

  override def work: F[Unit] =
    for {
      _ <- logger.info("initializing cache").pure[F]
      _ <- clusterCache.put(CacheEntry(0L, Seq.empty))
      _ <- logger.info("initialized cluster detail cache with nothing").pure[F]
    } yield ()

}

