package com.heimdali.actors

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.typesafe.config.Config
import org.apache.hadoop.conf.Configuration
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

object HiveActor {

  case class CreateUserDatabase(username: String)

  case class UserDatabaseCreated(database: HiveDatabase)

}

class HiveActor(configuration: Config,
                hadoopConfiguration: Configuration)
               (implicit val session: DBSession,
                val executionContext: ExecutionContext)
  extends Actor with ActorLogging {

  import HiveActor._

  val userDirectory: String = configuration.getString("hdfs.userRoot")
  val hdfsRoot: String = hadoopConfiguration.get("fs.default.name")

  def createDatabase(role: String, group: String, database: String, location: String): Future[HiveDatabase] =
    Future {
      log.info(s"creating a $database database and $role role for $group group at $hdfsRoot/$location")
      SQL(s"CREATE ROLE $role").execute().apply()
      SQL(s"GRANT ROLE $role TO GROUP $group").execute().apply()
      SQL(s"CREATE DATABASE $database LOCATION '$hdfsRoot/$location'").execute().apply()
      SQL(s"GRANT ALL ON DATABASE $database TO ROLE $role WITH GRANT OPTION").execute().apply()
      SQL(s"GRANT ALL ON URI '$hdfsRoot/$location' TO ROLE $role WITH GRANT OPTION").execute().apply()
      HiveDatabase(location, role, database)
    }

  override def receive: Receive = {
    case CreateUserDatabase(username) =>
      val role = s"role_$username"
      val group = s"edh_user_$username"
      val database = s"user_$username"
      val location = s"$userDirectory/$username/db"

      createDatabase(role, group, database, location)
        .map(UserDatabaseCreated)
        .pipeTo(sender())
  }
}

case class HiveDatabase(location: String, role: String, database: String)
