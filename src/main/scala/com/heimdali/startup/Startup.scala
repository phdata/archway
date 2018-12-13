package com.heimdali.startup

import cats._
import cats.effect._
import com.heimdali.config.{ClusterConfig, DatabaseConfig}

import scala.concurrent.ExecutionContext

trait Startup[F[_]] {
  def start()(implicit monadEvidence: Monad[F]): F[Unit]
}

class HeimdaliStartup[F[_] : Effect](databaseConfig: DatabaseConfig,
                                     clusterConfig: ClusterConfig,
                                     dbMigration: DBMigration[F],
                                     maintainer: SessionMaintainer[F])
                                    (implicit executionContext: ExecutionContext)
  extends Startup[F] {

  def start()(implicit monadEvidence: Monad[F]): F[Unit] =
    maintainer.setup

}