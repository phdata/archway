package io.phdata.startup

import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import io.phdata.config.DatabaseConfigItem
import org.flywaydb.core.Flyway

class SchemaMigration(metaConfig: DatabaseConfigItem) extends StrictLogging {

  def migrate(): Unit = {
    logger.info("Starting schema migration")

    val flyway = new Flyway()

    flyway.setValidateOnMigrate(true)

    flyway.setDataSource(metaConfig.url, metaConfig.username.get, metaConfig.password.get.value)

    val ddlPath = deriveDDLPath(metaConfig.url)
    logger.info(s"Using ddl path '$ddlPath''")
    flyway.setLocations(s"filesystem:$ddlPath")

    flyway.migrate
    logger.info("Finished schema migration")
  }

  private def deriveDDLPath(url: String) = {
    val ddlName = if (url.contains("postgres")) {
      "sql"
    } else if (url.contains("mysql")) {
      "mysql"
    } else if (url.contains("oracle")) {
      "oracle"
    } else {
      throw new RuntimeException(s"Unrecognized database type in URL '$url'. Could derive DDL path.")
    }

    logger.info(s"Chose DDL set: '$ddlName'.")

    s"flyway/$ddlName"
  }
}
