package com.heimdali.clients

import cats.effect.Sync
import cats.free.Free
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._

trait HiveClient[F[_]] {
  def createRole(name: String): F[Unit]

  def createDatabase(name: String, location: String): F[Unit]

  def grantGroup(group: String, role: String): F[Unit]

  def enableAccessToDB(database: String, role: String): F[Unit]

  def enableAccessToLocation(location: String, role: String): F[Unit]
}

class HiveClientImpl[F[_] : Sync](transactor: Transactor[F])
  extends HiveClient[F]
    with LazyLogging {

  override def createRole(name: String): F[Unit] = {
    sql"""SHOW ROLES""".query[String].to[Seq].transact(transactor).map {
      case roles if !roles.contains(name) =>
        (fr"CREATE ROLE" ++ Fragment.const(name)).update.run.transact(transactor)
      case _ =>
        Free.pure(())
    }.map(_ => ())
  }

  override def createDatabase(name: String, location: String): F[Unit] =
    sql"""SHOW DATABASES""".query[String].to[Seq].transact(transactor).map {
      case roles if !roles.contains(name) =>
        (fr"CREATE DATABASE" ++ Fragment.const(name) ++ fr"LOCATION '" ++ Fragment.const(location) ++ fr"'")
          .update.run.transact(transactor)
      case _ =>
        Free.pure(())
    }.map(_ => ())

  override def grantGroup(group: String, role: String): F[Unit] =
    (fr"GRANT ROLE" ++ Fragment.const(role) ++ fr"TO GROUP" ++ Fragment.const(group))
      .update.run.transact(transactor).map(_ => ())

  override def enableAccessToDB(database: String, role: String): F[Unit] =
    (fr"GRANT ALL ON DATABASE" ++ Fragment.const(database) ++ fr"TO ROLE" ++ Fragment.const(role) ++ fr"WITH GRANT OPTION")
      .update.run.transact(transactor).map(_ => ())

  override def enableAccessToLocation(location: String, role: String): F[Unit] =
    (fr"GRANT ALL ON URI '" ++ Fragment.const(location) ++ fr"' TO ROLE" ++ Fragment.const(role) ++ fr"WITH GRANT OPTION")
      .update.run.transact(transactor).map(_ => ())
}