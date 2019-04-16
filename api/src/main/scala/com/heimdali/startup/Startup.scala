package com.heimdali.startup

import cats.effect._
import cats.implicits._
import cats.effect.implicits._

import scala.concurrent.ExecutionContext

trait Startup[F[_]] {
  def start(): F[Unit]
}

class HeimdaliStartup[F[_] : ConcurrentEffect : ContextShift](jobs: ScheduledJob[F]*)
                                                             (executionContext: ExecutionContext)
  extends Startup[F] {

  def start(): F[Unit] =
    ContextShift[F].evalOn(executionContext)(jobs.toList.traverse(_.start())).start.void

}