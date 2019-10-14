package io.phdata.services

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.models.Yarn

trait YarnService[F[_]] {

  def updateYarnResources(yarn: Yarn, workspaceId: Long, instant: Instant): F[Unit]
}

class YarnServiceImpl[F[_]: Sync: Clock](context: AppContext[F]) extends YarnService[F] {

  override def updateYarnResources(
      yarn: Yarn,
      workspaceId: Long,
      instant: Instant
  ): F[Unit] = {
    context.yarnClient.setupPool(yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB) *>
      context.yarnRepository.update(yarn, workspaceId, instant).transact(context.transactor).void
  }
}
