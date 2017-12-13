package com.heimdali.startup

import org.flywaydb.core.Flyway

class FlywayMigration(flyway: Flyway) extends DBMigration {
  override def migrate(url: String, user: String, password: String): Int = {
    flyway.setDataSource(url, user, password)
    flyway.setValidateOnMigrate(false)
    flyway.migrate()
  }
}
