package com.heimdali.repositories

import com.heimdali.services.UserWorkspace
import doobie._
import doobie.implicits._
import cats.effect.IO

import scala.concurrent.{ExecutionContext, Future}

trait AccountRepository {
  def findUser(username: String): Future[Option[UserWorkspace]]

  def create(userWorkspace: UserWorkspace): Future[UserWorkspace]
}

class AccountRepositoryImpl(transactor: Transactor[IO])
                           (implicit executionContext: ExecutionContext) extends AccountRepository {

  def findUser(username: String): Future[Option[UserWorkspace]] =
    sql"""
         | SELECT
         |   username,
         |   database,
         |   data_directory,
         |   role
         | FROM
         |   users
         | WHERE
         |   username = $username""".stripMargin
      .query[UserWorkspace]
      .option
      .transact(transactor)
      .unsafeToFuture()

  def create(userWorkspace: UserWorkspace): Future[UserWorkspace] =
    sql"""
         | INSERT INTO
         |   users (
         |     username,
         |     database,
         |     data_directory,
         |     role
         |   )
         | VALUES
         |   (
         |     ${userWorkspace.username},
         |     ${userWorkspace.database},
         |     ${userWorkspace.dataDirectory},
         |     ${userWorkspace.role}
         |   )
         | """.stripMargin
      .update
      .run
      .transact(transactor)
      .unsafeToFuture()
      .map(_ => userWorkspace)

}