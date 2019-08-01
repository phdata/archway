package io.phdata.services

import cats.effect.{Async, Effect, IO, Sync}

trait LoginContextProvider {
  def kinit[F[_]: Sync](): F[Unit]

  def elevate[F[_]: Async, A](user: String)(block: () => A): F[A]

  def hadoopInteraction[F[_], A](block: F[A])(implicit F: Effect[F]): F[A]
}
