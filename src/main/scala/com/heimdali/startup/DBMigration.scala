package com.heimdali.startup

trait DBMigration {
  def migrate(url: String, user: String, password: String): Int
}
