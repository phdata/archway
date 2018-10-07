package com.heimdali.modules

import com.heimdali.startup._
import org.flywaydb.core.Flyway

trait StartupModule[F[_]] {
  this: AppModule[F]
    with ExecutionContextModule
    with ConfigurationModule
    with ContextModule[F] =>

  val flyway: Flyway = new Flyway()

  val dbMigration: DBMigration[F] =
    new FlywayMigration(flyway)

  val sessionMaintainer: SessionMaintainer[F] =
    new SessionMaintainerImpl[F](appConfig.cluster, loginContextProvider)

  val startup: Startup[F] =
    new HeimdaliStartup[F](appConfig.db, appConfig.cluster, dbMigration, sessionMaintainer)

}
