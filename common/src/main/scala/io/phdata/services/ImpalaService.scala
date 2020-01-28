package io.phdata.services

import cats.effect.Sync
import io.phdata.AppContext

trait ImpalaService {

  val TEMP_TABLE_NAME = "archway_temp"
  // Leaving this to clean up old temp tables running older versions
  val HEIMDALI_TEMP_TABLE_NAME = "heimdali_temp"

  def invalidateMetadata[F[_]: Sync](workspaceId: Long)(context: AppContext[F]): F[Unit]
}
