package com.heimdali.repositories

import cats.implicits._
import doobie._
import doobie.implicits._

class ConfigRepositoryImpl extends ConfigRepository {

  import Statements._

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  override def getValue(key: String): ConnectionIO[String] =
    select(key).unique

  override def setValue(key: String, value: String): ConnectionIO[Unit] =
    update(key, value).run.void

  object Statements {

    def select(key: String): Query0[String] =
      sql"""
          select config_value
          from heimdali_config
          where config_key = $key
      """.query[String]

    def update(key: String, value: String): Update0 =
      sql"""
            update heimdali_config
            set config_value = $value
            where config_key = $key
        """.update

  }

}
