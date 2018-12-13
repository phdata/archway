package com.heimdali.modules

import cats.effect.{ContextShift, Timer}

import scala.concurrent.ExecutionContext

trait ExecutionContextModule[F[_]] {

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  implicit def contextShift: ContextShift[F]

  implicit def timer: Timer[F]

}
