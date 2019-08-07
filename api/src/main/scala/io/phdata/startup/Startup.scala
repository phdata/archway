package io.phdata.startup

import cats.data._
import cats.effect._
import cats.effect.implicits._

import scala.concurrent.ExecutionContext

trait Startup[F[_]] {
  def begin(): F[NonEmptyList[Fiber[F, Unit]]]
}

class ArchwayStartup[F[_]: ConcurrentEffect: ContextShift](jobs: ScheduledJob[F]*)(executionContext: ExecutionContext)
    extends Startup[F] {

  val work: NonEmptyList[F[Unit]] =
    NonEmptyList.fromListUnsafe(jobs.toList.map(_.work))

  def begin(): F[NonEmptyList[Fiber[F, Unit]]] =
    ContextShift[F].evalOn(executionContext)(work.traverse(_.start))

}
