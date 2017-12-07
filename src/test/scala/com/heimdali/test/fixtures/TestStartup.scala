package com.heimdali.test.fixtures

import javax.inject.Inject

import com.heimdali.startup.{DBMigration, Startup}
import com.typesafe.config.Config

class TestStartup @Inject()(configuration: Config,
                            dbMigration: DBMigration) extends Startup {
  def start(): Unit = {
    val url = configuration.getString("ctx.url")
    val user = configuration.getString("ctx.user")
    val pass = configuration.getString("ctx.password")

    dbMigration.migrate(url, user, pass)
  }
}
