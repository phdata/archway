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
  private def createIfNotExists(resource: String, name: String, createSql: Fragment): Future[Option[Int]] =
    (fr"SHOW " ++ Fragment.const(s"${resource}S"))
      .query[String]
      .to[Seq]
      .transact(hiveTransactor)
      .unsafeToFuture()
      .flatMap {
        case existing if existing.contains(name) =>
          logger.info("{} {} already exists", resource, name)
          Future(None)
        case _ =>
          logger.info("creating {}: {}", resource, name)
          createSql
            .update
            .run
            .transact(hiveTransactor)
            .unsafeToFuture()
            .map(Some.apply)
      }

  override def createRole(name: String): Future[Option[Int]] =
    createIfNotExists("ROLE", name, fr"CREATE ROLE " ++ Fragment.const(name))

  override def createDatabase(name: String, location: String): Future[Option[Int]] =
    createIfNotExists("DATABASE", name, fr"CREATE DATABASE " ++ Fragment.const(name) ++ fr" LOCATION '" ++ Fragment.const(location) ++ fr"'")

  override def grantGroup(group: String, role: String): Future[Int] =
    (fr"GRANT ROLE " ++ Fragment.const(role) ++ fr" TO GROUP " ++ Fragment.const(group))
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()


  override def enableAccessToDB(database: String, role: String): Future[Int] =
    (fr"GRANT ALL ON DATABASE " ++ Fragment.const(database) ++ fr" TO ROLE " ++ Fragment.const(role) ++ fr" WITH GRANT OPTION")
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()

  override def enableAccessToLocation(location: String, role: String): Future[Int] =
    (fr"GRANT ALL ON URI '" ++ Fragment.const(location) ++ fr"' TO ROLE " ++ Fragment.const(role) ++ fr" WITH GRANT OPTION")
      .update
      .run
      .transact(hiveTransactor)
      .unsafeToFuture()
}