package com.heimdali.services

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.fragment.Fragment
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
                     (implicit executionContext: ExecutionContext)
  extends HiveService with LazyLogging {

  private def createIfNotExists(resource: String, name: String, createSql: Fragment): Future[Option[Int]] = Future {
    val query = fr"SHOW " ++ Fragment.const(s"${resource}S")
    val result = query
      .query[String]
      .to[Seq]
      .transact(hiveTransactor)
      .unsafeRunSync()

    result match {
      case existing if existing.contains(name) =>
        logger.info("{} {} already exists", resource, name)
        None
      case _ =>
        logger.info("creating {}: {}", resource, name)
        val outcome = createSql
          .update
          .run
          .transact(hiveTransactor)
          .unsafeRunSync()
        Some(outcome)
    }
  }

  override def createRole(name: String): Future[Option[Int]] = {
    logger.info("creating role {}", name)
    createIfNotExists("ROLE", name, fr"CREATE ROLE " ++ Fragment.const(name))
  }

  override def createDatabase(name: String, location: String): Future[Option[Int]] = {
    logger.info("creating database {} at {}", name, location)
    createIfNotExists("DATABASE", name, fr"CREATE DATABASE " ++ Fragment.const(name) ++ fr" LOCATION '" ++ Fragment.const(location) ++ fr"'")
  }

  override def grantGroup(group: String, role: String): Future[Int] = {
    logger.info("granting {} role to {} group", group, role)
    (fr"GRANT ROLE " ++ Fragment.const(role) ++ fr" TO GROUP " ++ Fragment.const(group))
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()
  }


  override def enableAccessToDB(database: String, role: String): Future[Int] = {
    logger.info("granting all on {} database to {} role", database, role)
    (fr"GRANT ALL ON DATABASE " ++ Fragment.const(database) ++ fr" TO ROLE " ++ Fragment.const(role) ++ fr" WITH GRANT OPTION")
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()
  }

  override def enableAccessToLocation(location: String, role: String): Future[Int] = {
    logger.info("granting all on {} location to {} role", location, role)
    (fr"GRANT ALL ON URI '" ++ Fragment.const(location) ++ fr"' TO ROLE " ++ Fragment.const(role) ++ fr" WITH GRANT OPTION")
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()
  }
}