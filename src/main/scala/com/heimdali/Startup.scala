package com.heimdali

import javax.inject.Inject

import org.flywaydb.core.Flyway
import play.api.Configuration

trait Startup {
}

class HeimdaliStartup @Inject() (configuration: Configuration) extends Startup {
  val flyway = new Flyway()
  flyway.setDataSource(configuration.get[String]("ctx.url"), configuration.get[String]("ctx.user"), configuration.get[String]("ctx.password"))
  flyway.setValidateOnMigrate(false)
  flyway.migrate()
}