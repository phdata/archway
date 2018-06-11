package com.heimdali.modules

import com.heimdali.startup._
import org.flywaydb.core.Flyway

trait StartupModule[F[_]] {
  this: ExecutionContextModule
    with ConfigurationModule
    with ContextModule[F] =>

  val flyway: Flyway = new Flyway()

  val dbMigration: DBMigration[F] =
    new FlywayMigration(flyway)

  val sessionMaintainer: SessionMaintainer = new SessionMaintainerImpl(appConfig.cluster, loginContextProvider)

  val startup: Startup[F] =
    new HeimdaliStartup[F](appConfig.db, appConfig.cluster, dbMigration, sessionMaintainer)

}
