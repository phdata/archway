package com.heimdali.services

import cats.effect.{Async, IO}

trait LoginContextProvider {
  def kinit(): IO[Unit]

  def elevate[F[_] : Async, A](user: String)(block: () => A): F[A]
}
