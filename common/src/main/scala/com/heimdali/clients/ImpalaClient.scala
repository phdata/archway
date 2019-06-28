package com.heimdali.clients

import cats.effect.Effect
import cats.implicits._
import com.heimdali.repositories.CustomLogHandler
import com.heimdali.services.LoginContextProvider
import com.typesafe.scalalogging.LazyLogging
import doobie.{Transactor, _}
import doobie.implicits._


trait ImpalaClient[F[_]] {
  def invalidateMetadata(database: String, tableName: String): F[Unit]
}

class ImpalaClientImpl[F[_]](
    loginContextProvider: LoginContextProvider,
    transactor: Transactor[F])(implicit val F: Effect[F])
    extends ImpalaClient[F] with LazyLogging {
  implicit val logHandler = CustomLogHandler.logHandler(this.getClass)


  override def invalidateMetadata(database: String, tableName: String): F[Unit] = {
    loginContextProvider.hadoopInteraction[F, Unit] {
      (sql"""INVALIDATE metadata """ ++ Fragment.const(s"$database.$tableName")).update.run.transact(transactor).void
    }
  }
}
