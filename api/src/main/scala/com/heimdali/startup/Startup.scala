package com.heimdali.startup

import cats.effect.Sync
import cats.implicits._

import scala.concurrent.ExecutionContext

trait Startup[F[_]] {
  def start(): F[Unit]
}

class HeimdaliStartup[F[_]: Sync](jobs: ScheduledJob[F]*)
                                    (executionContext: ExecutionContext)
  extends Startup[F] {

  def start(): F[Unit] =
    jobs.toList.traverse(_.start()).void

}