package com.heimdali.startup

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

trait Startup {
  def start(): Future[Unit]
}

class HeimdaliStartup(configuration: Config,
                      dbMigration: DBMigration)
                     (implicit executionContext: ExecutionContext)
  extends Startup with LazyLogging {

  def start(): Future[Unit] = Future {
    val url = configuration.getString("ctx.url")
    val user = configuration.getString("ctx.user")
    val pass = configuration.getString("ctx.password")

    dbMigration.migrate(url, user, pass)
  }

}