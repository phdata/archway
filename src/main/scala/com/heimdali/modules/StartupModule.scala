package com.heimdali.modules

import cats.effect.Effect
import com.heimdali.startup._
import org.flywaydb.core.Flyway

trait StartupModule[F[_]] {
  this: ExecutionContextModule[F]
    with ConfigurationModule
    with ContextModule[F] =>

  implicit def effect: Effect[F]

  val flyway: Flyway = new Flyway()

  val dbMigration: DBMigration[F] =
    new FlywayMigration(flyway)

  val sessionMaintainer: SessionMaintainer[F] =
    new SessionMaintainerImpl[F](appConfig.cluster, loginContextProvider)

  val startup: Startup[F] =
    new HeimdaliStartup[F](appConfig.db, appConfig.cluster, dbMigration, sessionMaintainer)

}
