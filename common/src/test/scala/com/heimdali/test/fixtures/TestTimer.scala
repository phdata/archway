package com.heimdali.test.fixtures

import java.time.Instant

import cats.effect.{Clock, IO, Timer}

import scala.concurrent.duration.FiniteDuration

class TestTimer extends Timer[IO] {
  private val millis: Long =
    java.lang.System.currentTimeMillis()

  val instant: Instant =
    Instant.ofEpochMilli(millis)

  override def clock: Clock[IO] =
    new TestClock(millis)

  override def sleep(duration: FiniteDuration): IO[Unit] =
    IO.unit
}
