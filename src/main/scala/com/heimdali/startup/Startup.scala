package com.heimdali.startup

import cats.{Applicative, Monad}
import cats.data.OptionT
import com.heimdali.config.{ClusterConfig, DatabaseConfig}

trait Startup[F[_]] {
  def start()(implicit monadEvidence: Monad[F]): F[Unit]
}

class HeimdaliStartup[F[_]](databaseConfig: DatabaseConfig,
                            clusterConfig: ClusterConfig,
                            dbMigration: DBMigration[F],
                            maintainer: SessionMaintainer)
  extends Startup[F] {

  def start()(implicit monadEvidence: Monad[F]): F[Unit] =
    (for {
      _ <- OptionT.fromOption(for {
        user <- databaseConfig.meta.username
        pass <- databaseConfig.meta.password
      } yield dbMigration.migrate(databaseConfig.meta.url, user, pass))
      _ <- OptionT.some(maintainer.setup)
    } yield ()).getOrElse(())

}