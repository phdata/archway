package com.heimdali.startup

import cats.Apply
import cats.effect._
import cats.implicits._

import scala.concurrent.ExecutionContext

trait Startup[F[_]] {
  def begin(): F[Unit]
}

class HeimdaliStartup[F[_] : Async : ConcurrentEffect : ContextShift](provisionJob: Provisioning[F],
                                                                      sessionMaintainer: SessionMaintainer[F])
                                                                     (executionContext: ExecutionContext)
  extends Startup[F] {

  lazy val merged: fs2.Stream[F, Unit] =
    sessionMaintainer.stream merge provisionJob.stream

  def begin(): F[Unit] =
    Apply[F].productR(
      sessionMaintainer.kinit()
    )(
      ContextShift[F].evalOn(executionContext)(merged.compile.drain.void)
    )

}