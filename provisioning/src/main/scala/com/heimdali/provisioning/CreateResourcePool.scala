package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Effect, Sync}
import cats.implicits._
import doobie.Transactor
import doobie.implicits._

case class CreateResourcePool(id: Long, name: String, cores: Int, memory: Int)

object CreateResourcePool {

  implicit val show: Show[CreateResourcePool] =
    Show.show(c => s"creating resource pool ${c.name} with ${c.cores} cores and ${c.memory} memory")

  implicit object CreateResourcePoolCompletionTask extends CompletionTask[CreateResourcePool] {

    override def apply[F[_] : Sync](createResourcePool: CreateResourcePool, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.yarnRepository.complete(createResourcePool.id, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object CreateResourcePoolProvisioningTask extends ProvisioningTask[CreateResourcePool] {

    override def apply[F[_] : Sync : Clock](createResourcePool: CreateResourcePool, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.yarnClient.createPool(createResourcePool.name, createResourcePool.cores, createResourcePool.memory)

  }

  implicit val provisionable: Provisionable[CreateResourcePool] = Provisionable.deriveProvisionable

}
