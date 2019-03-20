package com.heimdali.test.fixtures

import cats.effect.{Clock, IO}

import scala.concurrent.duration.TimeUnit

class TestClock(instant: Long) extends Clock[IO] {

  override def realTime(unit: TimeUnit): IO[Long] =
    IO(instant)

  override def monotonic(unit: TimeUnit): IO[Long] =
    IO(instant)
}
