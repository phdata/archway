package com.heimdali.startup

import javax.inject.Inject

import play.api.Configuration

trait Startup {
  def start(): Unit

  start()
}

class HeimdaliStartup @Inject()(configuration: Configuration,
                        dbMigration: DBMigration,
                        securityContext: SecurityContext) extends Startup {
  def start(): Unit = {
    val url = configuration.get[String]("ctx.url")
    val user = configuration.get[String]("ctx.user")
    val pass = configuration.get[String]("ctx.password")

    dbMigration.migrate(url, user, pass)

    val adminUser = configuration.get[String]("kerberos.username")
    val adminPassword = configuration.get[String]("kerberos.password")

    securityContext.login(adminUser, adminPassword)
  }
}