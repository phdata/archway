package com.heimdali._db

import com.heimdali.startup.FlywayMigration
import com.typesafe.config.ConfigFactory
import org.flywaydb.core.Flyway
import org.scalatest.FlatSpec

class DBMigrationSpec extends FlatSpec {

  behavior of "DB Migration"

  ignore should "migrate the database" in {
    val config = ConfigFactory.defaultApplication()
    val meta = config.getConfig("db.meta")
    val flyway = new Flyway()
    val migration = new FlywayMigration(flyway)
    migration.migrate(meta.getString("url"), meta.getString("user"), meta.getString("password"))
  }

}
