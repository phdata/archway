package com.heimdali.startup

import com.typesafe.config.Config

trait Startup {
  def start(): Unit
}

class HeimdaliStartup(configuration: Config,
                      dbMigration: DBMigration,
                      securityContext: SecurityContext) extends Startup {
  def start(): Unit = {
    val url = configuration.getString("ctx.url")
    val user = configuration.getString("ctx.user")
    val pass = configuration.getString("ctx.password")

    dbMigration.migrate(url, user, pass)

    val adminUser = configuration.getString("kerberos.username")
    val adminPassword = configuration.getString("kerberos.password")

    securityContext.login(adminUser, adminPassword)
  }
}