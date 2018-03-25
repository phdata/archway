package com.heimdali.services

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.concurrent.{ExecutionContext, Future}

trait HiveService {
  def createRole(name: String): Future[Option[Int]]

  def createDatabase(name: String, location: String): Future[Option[Int]]

  def grantGroup(group: String, role: String): Future[Int]

  def enableAccessToDB(database: String, role: String): Future[Int]

  def enableAccessToLocation(location: String, role: String): Future[Int]
}

class HiveServiceImpl(hiveTransactor: Transactor[IO])
                     (implicit executionContext: ExecutionContext) extends HiveService {
  private def createIfNotExists(resource: String, name: String, createSql: String): Future[Option[Int]] =
    sql"SHOW ${resource}S".query[String].to[Seq].transact(hiveTransactor).unsafeToFuture().flatMap {
      case existing if existing.contains(name) =>
        Future(None)
      case _ =>
        sql"$createSql".update.run.transact(hiveTransactor).unsafeToFuture().map(Some.apply)
    }

  override def createRole(name: String): Future[Option[Int]] =
    createIfNotExists("ROLE", name, s"CREATE ROLE $name")

  override def createDatabase(name: String, location: String): Future[Option[Int]] =
    createIfNotExists("DATABASE", name, s"CREATE DATABASE $name LOCATION '$location'")

  override def grantGroup(group: String, role: String): Future[Int] =
    sql"GRANT ROLE $role TO GROUP $group"
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()


  override def enableAccessToDB(database: String, role: String): Future[Int] =
    sql"GRANT ALL ON DATABASE $database TO ROLE $role WITH GRANT OPTION"
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()

  override def enableAccessToLocation(location: String, role: String): Future[Int] =
    sql"GRANT ALL ON URI '$location' TO ROLE $role WITH GRANT OPTION"
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()
}