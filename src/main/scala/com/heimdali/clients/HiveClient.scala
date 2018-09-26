package com.heimdali.clients

import cats.data.OptionT
import cats.effect.{Effect, Sync}
import cats.implicits._
import com.heimdali.models.{HiveDatabase, HiveTable}
import com.heimdali.services.LoginContextProvider
import doobie._
import doobie.implicits._

trait HiveClient[F[_]] {

  def createDatabase(name: String, location: String): F[Unit]

  def describeDatabase(name: String): F[HiveDatabase]

}

class HiveClientImpl[F[_]](loginContextProvider: LoginContextProvider,
                           transactor: Transactor[F])
                          (implicit val F: Effect[F])
  extends HiveClient[F] {

  override def createDatabase(name: String, location: String): F[Unit] =
    loginContextProvider.hadoopInteraction {
      sql"""SHOW DATABASES""".query[String].to[Seq].transact(transactor).flatMap {
        case roles if !roles.contains(name) =>
          (fr"CREATE DATABASE" ++ Fragment.const(name) ++ fr"LOCATION " ++ Fragment.const(s"'$location'"))
            .update.run.transact(transactor).void
        case _ =>
          Sync[F].unit
      }
    }

  override def describeDatabase(name: String): F[HiveDatabase] =
    loginContextProvider.hadoopInteraction[F, HiveDatabase] {
      (sql"""SHOW TABLES in """ ++ Fragment.const(name)).query[String].to[List].transact(transactor).map { tables =>
        HiveDatabase(name, tables.map(HiveTable.apply))
      }
    }
}