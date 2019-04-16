package com.heimdali.clients

import cats.effect.{Effect, Sync}
import cats.implicits._
import com.heimdali.models.{HiveDatabase, HiveTable}
import com.heimdali.services.LoginContextProvider
import doobie._
import doobie.implicits._

trait HiveClient[F[_]] {

  def createDatabase(name: String, location: String, comment: String, dbProperties: Map[String, String]): F[Unit]

  def describeDatabase(name: String): F[HiveDatabase]

  def showDatabases(): F[Seq[String]]

}

class HiveClientImpl[F[_]](loginContextProvider: LoginContextProvider,
                           transactor: Transactor[F])
                          (implicit val F: Effect[F])
  extends HiveClient[F] {

  implicit val logHandler = LogHandler.jdkLogHandler

  override def createDatabase(name: String, location: String, comment: String, dbProperties: Map[String, String]): F[Unit] =
    loginContextProvider.hadoopInteraction {
      showDatabases().flatMap {
        case databases if !databases.contains(name) =>
          (createDatabaseStatement(name, location, comment, dbProperties)
            .update.run.transact(transactor).void)
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

  private[clients] def createDatabaseStatement(name: String, location: String, comment: String, dbProperties: Map[String, String]): Fragment = {
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
      val propertyPairs = dbProperties.map {
        case (key, value) =>
          fr"$key = $value"
      }.reduce((l, r) => l ++ fr"," ++ r)

      fr"with DBPROPERTIES(" ++ propertyPairs ++ fr")"
    }
}
