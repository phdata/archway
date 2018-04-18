package com.heimdali.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.heimdali.services.HiveService
import com.typesafe.config.Config
import org.apache.hadoop.conf.Configuration

import scala.concurrent.{ExecutionContext, Future}

object HiveActor {

  case class CreateUserDatabase(username: String)

  case class CreateSharedDatabase(name: String)

  case class DatabaseCreated(database: HiveDatabase)

  def props(configuration: Config,
            hadoopConfiguration: Configuration,
            hiveService: HiveService)
           (implicit executionContext: ExecutionContext) =
    Props(new HiveActor(configuration, hadoopConfiguration, hiveService))

}

class HiveActor(configuration: Config,
                hadoopConfiguration: Configuration,
                hiveService: HiveService)
               (implicit val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import HiveActor._

  val userDirectory: String = configuration.getString("hdfs.userRoot")
  val projectDirectory: String = configuration.getString("hdfs.projectRoot")
  val hdfsRoot: String = hadoopConfiguration.get("fs.default.name")

  def createDatabase(role: String, group: String, database: String, location: String): Future[HiveDatabase] = {
    log.info(s"creating a $database database and $role role for $group group at $hdfsRoot/$location")
    val dataDirectory = s"$hdfsRoot/$location"
    for (
      _ <- hiveService.createRole(role);
      _ <- hiveService.grantGroup(group, role);
      _ <- hiveService.createDatabase(database, dataDirectory);
      _ <- hiveService.enableAccessToDB(database, role);
      _ <- hiveService.enableAccessToLocation(dataDirectory, role)
    ) yield HiveDatabase(location, role, database)
  }

  override def receive: Receive = {
    case CreateUserDatabase(username) =>
      val role = s"role_$username"
      val group = s"edh_user_$username"
      val database = s"user_$username"
      val location = s"$userDirectory/$username/db"

      createDatabase(role, group, database, location)
        .map(DatabaseCreated)
        .pipeTo(sender())

    case CreateSharedDatabase(name) =>
      val role = s"role_sw_$name"
      val group = s"edh_sw_$name"
      val database = s"sw_$name"
      val location = s"$projectDirectory/$name"

      createDatabase(role, group, database, location)
        .map(DatabaseCreated)
        .recover {
          case ex: Throwable =>
            ex.printStackTrace()
            DatabaseCreated(null)
        }
        .pipeTo(sender())
  }
}

case class HiveDatabase(location: String, role: String, name: String)
