package io.phdata.services

import java.time.Instant

import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.models.Yarn

trait YarnService[F[_]] {

  def updateYarnResources(yarn: Yarn, resourcePoolId: Long, instant: Instant): F[Unit]

  def list(workspaceId: Long): F[List[Yarn]]
}

class YarnServiceImpl[F[_]: Sync: Clock](context: AppContext[F]) extends YarnService[F] {

  override def updateYarnResources(
      yarn: Yarn,
      resourcePoolId: Long,
      instant: Instant
  ): F[Unit] = {
    context.yarnClient.setupPool(yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB) *>
      context.yarnRepository.update(yarn, resourcePoolId, instant).transact(context.transactor).void
  }

  override def list(workspaceId: Long): F[List[Yarn]] =
    context.yarnRepository.findByWorkspaceId(workspaceId).transact(context.transactor)
}
