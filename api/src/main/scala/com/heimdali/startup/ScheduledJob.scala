/* Copyright 2018 phData Inc. */

package com.heimdali.startup

import cats.effect.{Sync, Timer}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.FiniteDuration

trait ScheduledJob[F[_]] {

  def start(): F[Unit]

}

object ScheduledJob extends LazyLogging {
  def onInterval[F[_] : Sync : Timer](job: () => F[Unit], interval: FiniteDuration): F[Unit] =
    fs2.Stream.awakeEvery[F](interval)
      .evalMap { _ =>
        logger.info("running scheduled job: {}", job)
        job()
      }
      .compile
      .drain

}
