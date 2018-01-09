package com.heimdali.test.fixtures

import com.heimdali.startup.{DBMigration, Startup}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class TestStartup(configuration: Config,
                  dbMigration: DBMigration)
                 (implicit executionContext: ExecutionContext)
  extends Startup {
  def start(): Future[Unit] = Future {
    val url = configuration.getString("ctx.url")
    val user = configuration.getString("ctx.user")
    val pass = configuration.getString("ctx.password")

    dbMigration.migrate(url, user, pass)
  }
}
