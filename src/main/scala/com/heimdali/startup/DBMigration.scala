package com.heimdali.startup

import cats.Applicative

trait DBMigration[F[_]] {
  def migrate(url: String, user: String, password: String)(implicit appEvidence: Applicative[F]): F[Unit]
}
