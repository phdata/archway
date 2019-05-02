/* Copyright 2018 phData Inc. */

package com.heimdali.startup


import cats.effect.{ContextShift, Sync, Timer}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.FiniteDuration

trait ScheduledJob[F[_]] {

  def stream: fs2.Stream[F, Unit]

}

object ScheduledJob extends LazyLogging {

  def onInterval[F[_] : Sync : Timer : ContextShift](job: () => F[Unit], interval: FiniteDuration): fs2.Stream[F, Unit] =
    fs2.Stream.awakeEvery[F](interval)
      .evalMap { _ =>
        logger.info("running scheduled job: {}", job)
        job()
      }

}
