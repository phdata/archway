package com.heimdali.startup

import cats.Applicative
import org.flywaydb.core.Flyway

class FlywayMigration[F[_]](flyway: Flyway) extends DBMigration[F] {
  override def migrate(url: String, user: String, password: String)
                      (implicit appEvidence: Applicative[F]): F[Unit] = appEvidence.pure {
//    flyway.setDataSource(url, user, password)
//    flyway.setValidateOnMigrate(false)
//    flyway.migrate()
    ()
  }
}
