package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.services.ClusterService
import com.typesafe.scalalogging.LazyLogging

class CacheInitializer[F[_] : Sync : ContextShift : Timer](clusterService: ClusterService[F])
  extends ScheduledJob[F] with LazyLogging {

  override def work: F[Unit] =
    for {
      _ <- logger.info("initializing cache").pure[F]
      initialResult <- clusterService.list
      _ <- logger.info("initialized cluster detail cache with {}", initialResult).pure[F]
    } yield ()

}

