package io.phdata.services

import java.util.concurrent.atomic.AtomicBoolean

import cats.Eval
import cats.effect.concurrent.{Ref, Semaphore}
import cats.effect.{Async, IO, Sync}
import cats.implicits._

import scala.concurrent.{ExecutionContext, Promise}


class Memo[F[_]: Sync : Async](implicit val ec: ExecutionContext) {

  def memoize[A](io: IO[A])(implicit ec: ExecutionContext): IO[IO[A]] =
    IO { IO.fromFuture(IO.eval(Eval.later(io.unsafeToFuture()))) }

}
