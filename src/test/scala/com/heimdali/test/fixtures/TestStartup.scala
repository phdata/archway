package com.heimdali.test.fixtures

import javax.inject.Inject

import com.heimdali.startup.{DBMigration, Startup}
import play.api.Configuration

class TestStartup @Inject()(configuration: Configuration,
                            dbMigration: DBMigration) extends Startup {
  def start(): Unit = {
    val url = configuration.get[String]("ctx.url")
    val user = configuration.get[String]("ctx.user")
    val pass = configuration.get[String]("ctx.password")

    dbMigration.migrate(url, user, pass)
  }
}
