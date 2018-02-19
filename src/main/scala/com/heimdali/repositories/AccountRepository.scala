package com.heimdali.repositories

import com.heimdali.services.UserWorkspace
import io.getquill._

import scala.concurrent.{ExecutionContext, Future}

trait AccountRepository {
  def findUser(username: String): Future[Option[UserWorkspace]]

  def create(userWorkspace: UserWorkspace): Future[UserWorkspace]
}

class AccountRepositoryImpl(implicit executionContext: ExecutionContext) extends AccountRepository {
  val context = new PostgresAsyncContext(NamingStrategy(SnakeCase, PluralizedTableNames), "ctx") with ImplicitQuery

  import context._

  val userQuery = quote {
    querySchema[UserWorkspace]("users")
  }

  def findUser(username: String): Future[Option[UserWorkspace]] = run(quote {
    userQuery.filter(_.username == lift(username))
  }).map(_.headOption)

  def create(userWorkspace: UserWorkspace): Future[UserWorkspace] = {
    run {
      userQuery.insert(lift(userWorkspace))
    }.map(_ => userWorkspace)
  }

}