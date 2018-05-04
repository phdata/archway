package com.heimdali.services

import java.sql.Connection

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

trait HiveService {
  def createRole(name: String)(implicit connection: Connection): Future[Option[Boolean]]

  def createDatabase(name: String, location: String)(implicit connection: Connection): Future[Option[Boolean]]

  def grantGroup(group: String, role: String)(implicit connection: Connection): Future[Boolean]

  def enableAccessToDB(database: String, role: String)(implicit connection: Connection): Future[Boolean]

  def enableAccessToLocation(location: String, role: String)(implicit connection: Connection): Future[Boolean]
}

class HiveServiceImpl(implicit executionContext: ExecutionContext)
  extends HiveService with LazyLogging {

  def runCommand(sql: String, connection: Connection): Future[Boolean] = Future {
    logger.info(s"running $sql against hive")
    val statement = connection.prepareStatement(sql);
    statement.execute()
  }

  def getString(sql: String, connection: Connection): Future[Seq[String]] = Future {
    logger.info(s"running $sql against hive")
    val statement = connection.prepareStatement(sql)
    val resultSet = statement.executeQuery()
    new Iterator[String] {
      def hasNext: Boolean = resultSet.next()

      def next(): String = resultSet.getString(1)
    }.toStream.toSeq
  }

  override def createRole(name: String)(implicit connection: Connection): Future[Option[Boolean]] = {
    getString("SHOW ROLES", connection)
      .map(_.find(_ == name))
      .flatMap {
        case Some(_) =>
          logger.info("{} role already exists", name)
          Future(None)
        case None =>
          logger.info("creating role {}", name)
          runCommand(s"CREATE ROLE ${name}", connection).map(Some(_))
      }
      .recover {
        case exc: Throwable =>
          logger.error("couldn't create role {}", name)
          logger.error(exc.getMessage, exc)
          throw exc
      }
  }

  override def createDatabase(name: String, location: String)(implicit connection: Connection): Future[Option[Boolean]] = {
    getString("SHOW DATABASES", connection)
      .map(_.find(_ == name))
      .flatMap {
        case Some(_) =>
          logger.info("{} database already exists", name)
          Future(None)
        case None =>
          logger.info("creating database {}", name)
          runCommand(s"CREATE DATABASE $name LOCATION '$location'", connection).map(Some(_))
      }
      .recover {
        case exc: Throwable =>
          logger.error("couldn't create role {}", name)
          logger.error(exc.getMessage, exc)
          throw exc
      }
  }

  override def grantGroup(group: String, role: String)(implicit connection: Connection): Future[Boolean] = {
    logger.info("granting {} role to {} group", group, role)
    runCommand(s"GRANT ROLE $role TO GROUP $group", connection)
      .recover {
        case exc: Throwable =>
          logger.error("couldn't grant role {} to group {}", role, group)
          logger.error(exc.getMessage, exc)
          throw exc
      }
  }


  override def enableAccessToDB(database: String, role: String)(implicit connection: Connection): Future[Boolean] = {
    logger.info("granting all on {} database to {} role", database, role)
    runCommand(s"GRANT ALL ON DATABASE $database TO ROLE $role WITH GRANT OPTION", connection)
      .recover {
        case exc: Throwable =>
          logger.error("couldn't enable access for {} to {}", role, database)
          logger.error(exc.getMessage, exc)
          throw exc
      }
  }

  override def enableAccessToLocation(location: String, role: String)(implicit connection: Connection): Future[Boolean] = {
    logger.info("granting all on {} location to {} role", location, role)
    runCommand(s"GRANT ALL ON URI '$location' TO ROLE $role WITH GRANT OPTION", connection)
      .recover {
        case exc: Throwable =>
          logger.error("couldn't enable {} access to {}", role, location)
          logger.error(exc.getMessage, exc)
          throw exc
      }
  }
}