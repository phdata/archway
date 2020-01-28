package io.phdata.services

import cats.effect.Sync
import cats.implicits._
import io.phdata.AppContext
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._

object ImpalaServiceImpl extends ImpalaService with LazyLogging {

  def invalidateMetadata[F[_]: Sync](workspaceId: Long)(context: AppContext[F]): F[Unit] = {
    for {
      _ <- logger.debug(s"Invalidate metadata started for workspace id '$workspaceId'").pure[F]
      allocations <- context.databaseRepository.findByWorkspace(workspaceId).transact(context.transactor)
      _ <- allocations.traverse(x => ImpalaServiceImpl.invalidateMetadata(x.name)(context))
    } yield ()
  }

  private def invalidateMetadata[F[_]: Sync](database: String)(context: AppContext[F]): F[Unit] = {
    for {
      result <- context.hiveClient.createTable(database, TEMP_TABLE_NAME)
      _ <- logger.info("Create table result " + result).pure[F]
      _ <- context.impalaClient
        .map(_.invalidateMetadata(database, TEMP_TABLE_NAME).attempt.flatMap {
          case Right(_) => logger.info(s"Invalidate metadata for '$database' complete").pure[F]
          case Left(e)  => logger.error(s"Impala metadata invalidation failed: ${e.getLocalizedMessage}", e).pure[F]
        })
        .getOrElse(
          logger
            .warn(s"Skipped Impala invalidate metadata for database '$database' because ImpalaClient was not defined")
            .pure[F]
        )
      _ <- context.hiveClient.dropTable(database, TEMP_TABLE_NAME)
      _ <- context.hiveClient.dropTable(database, HEIMDALI_TEMP_TABLE_NAME)
    } yield ()
  }

}
