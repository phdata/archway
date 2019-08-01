package io.phdata.clients

import cats.effect.{Effect, Sync}
import cats.implicits._
import io.phdata.models.{HiveDatabase, HiveTable}
import io.phdata.repositories.CustomLogHandler
import io.phdata.services.LoginContextProvider
import doobie._
import doobie.implicits._
import io.phdata.models.HiveDatabase

trait HiveClient[F[_]] {

  def createDatabase(name: String, location: String, comment: String, dbProperties: Map[String, String]): F[Unit]

  def describeDatabase(name: String): F[HiveDatabase]

  def showDatabases(): F[Seq[String]]

  def dropDatabase(name: String): F[Int]

  def createTable(database: String, name: String): F[Int]

  def showTables(databaseName: String): F[Seq[String]]

  def dropTable(databaseName: String, name: String): F[Unit]

}

class HiveClientImpl[F[_]](loginContextProvider: LoginContextProvider, transactor: Transactor[F])(
    implicit val F: Effect[F]
) extends HiveClient[F] {

  implicit val logHandler = CustomLogHandler.logHandler(this.getClass)

  override def createDatabase(
      name: String,
      location: String,
      comment: String,
      dbProperties: Map[String, String]
  ): F[Unit] =
    loginContextProvider.hadoopInteraction {
      showDatabases().flatMap {
        case databases if !databases.contains(name) =>
          (createDatabaseStatement(name, location, comment, dbProperties).update.run.transact(transactor).void)
        case _ =>
          Sync[F].unit
      }
    }

  override def showDatabases() = {
    sql"""SHOW DATABASES""".query[String].to[Seq].transact(transactor)
  }

  override def describeDatabase(name: String): F[HiveDatabase] =
    loginContextProvider.hadoopInteraction[F, HiveDatabase] {
      (sql"""SHOW TABLES in """ ++ Fragment.const(name)).query[String].to[List].transact(transactor).map { tables =>
        HiveDatabase(name, tables.map(HiveTable.apply))
      }
    }

  override def dropDatabase(name: String): F[Int] = {
    loginContextProvider.hadoopInteraction[F, Int] {
      (sql"""DROP DATABASE IF EXISTS """ ++ Fragment.const(name) ++ fr"CASCADE").update.run.transact(transactor)
    }
  }

  override def createTable(database: String, name: String): F[Int] = {
    loginContextProvider.hadoopInteraction[F, Int] {
      (sql"""CREATE TABLE IF NOT EXISTS """ ++ Fragment.const(database) ++ fr"." ++ Fragment.const(name) ++
        fr"(id SMALLINT)").update.run.transact(transactor)
    }
  }

  override def showTables(databaseName: String): F[Seq[String]] = {
    (sql"""SHOW TABLES in """ ++ Fragment.const(databaseName)).query[String].to[Seq].transact(transactor)

  }

  override def dropTable(databaseName: String, name: String): F[Unit] = {
    (sql"""DROP TABLE IF EXISTS """ ++ Fragment.const(databaseName) ++ fr"." ++ Fragment.const(name)).update.run
      .transact(transactor)
      .void
  }

  private[clients] def createDatabaseStatement(
      name: String,
      location: String,
      comment: String,
      dbProperties: Map[String, String]
  ): Fragment = {
    fr"CREATE DATABASE" ++ Fragment.const(name) ++ Fragment.const(" COMMENT ") ++
      fr"$comment" ++
      Fragment.const("LOCATION ") ++
      fr"$location" ++
      createDBPropertiesFragment(dbProperties)
  }

  private[clients] def createDBPropertiesFragment(dbProperties: Map[String, String]) =
    if (dbProperties.isEmpty) {
      fr""
    } else {
      val propertyPairs = dbProperties
        .map {
          case (key, value) =>
            fr"$key = $value"
        }
        .reduce((l, r) => l ++ fr"," ++ r)

      fr"with DBPROPERTIES(" ++ propertyPairs ++ fr")"
    }
}
