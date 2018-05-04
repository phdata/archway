package com.heimdali.provisioning

import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.heimdali.models.HiveDatabase
import com.heimdali.services.HiveService
import com.typesafe.config.Config
import org.apache.hadoop.conf.Configuration
import scalikejdbc._

import scala.concurrent.ExecutionContext

object HiveActor {

  case class CreateDatabase(ownerGroup: String, databaseName: String, role: String, dataDirectory: String)

  case class DatabaseCreated(database: HiveDatabase)

  def props(configuration: Config,
            hadoopConfiguration: Configuration,
            hiveService: HiveService,
            connectionFactory: () => Connection)
           (implicit executionContext: ExecutionContext) =
    Props(new HiveActor(configuration, hadoopConfiguration, hiveService, connectionFactory))

}

class HiveActor(configuration: Config,
                hadoopConfiguration: Configuration,
                hiveService: HiveService,
                connectionFactory: () => Connection)
               (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import HiveActor._

  override def receive: Receive = {
    case CreateDatabase(ownerGroup: String, databaseName, role, dataDirectory) =>
      log.info(s"creating a $databaseName database and $role role for $ownerGroup group at $dataDirectory")
      val connection = connectionFactory()
      (for (
        _ <- hiveService.createRole(role)(connection);
        _ <- hiveService.grantGroup(ownerGroup, role)(connection);
        _ <- hiveService.createDatabase(databaseName, dataDirectory)(connection);
        _ <- hiveService.enableAccessToDB(databaseName, role)(connection);
        _ <- hiveService.enableAccessToLocation(dataDirectory, role)(connection)
      ) yield HiveDatabase(None, databaseName, role, dataDirectory, 0))
        .map(DatabaseCreated)
        .recover {
          case exc: Throwable =>
            log.error(exc, "Couldn't create database")
            exc
        }
        .pipeTo(sender())
  }
}
