package com.heimdali.startup

import org.flywaydb.core.Flyway

class FlywayMigration extends DBMigration {
  override def migrate(url: String, user: String, password: String): Int = {
    val flyway = new Flyway()
    flyway.setDataSource(url, user, password)
    flyway.setValidateOnMigrate(false)
    flyway.migrate()
  }
}
