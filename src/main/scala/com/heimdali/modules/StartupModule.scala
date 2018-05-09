package com.heimdali.modules

import com.heimdali.startup.{DBMigration, FlywayMigration, HeimdaliStartup, Startup}
import org.flywaydb.core.Flyway

trait StartupModule {
  this: ConfigurationModule
    with ExecutionContextModule
    with ContextModule =>

  val flyway: Flyway = new Flyway()

  val dbMigration: DBMigration =
    new FlywayMigration(flyway)

  val startup: Startup =
    new HeimdaliStartup(configuration, dbMigration, loginContextProvider)

}
