package com.heimdali.repositories

import doobie._

trait ConfigRepository {

  def getValue(key: String): ConnectionIO[String]

  def setValue(key: String, value: String): ConnectionIO[Unit]

}
