package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class ResourcePoolRegistration(id: Long, name: String, cores: Int, memory: Int)

object ResourcePoolRegistration {

  implicit val show: Show[ResourcePoolRegistration] =
    Show.show(c => s"""resource pool "${c.name}" with ${c.cores} cores and ${c.memory}gb memory""")

  implicit object ResourcePoolRegistrationProvisioningTask extends ProvisioningTask[ResourcePoolRegistration] {

    override def complete[F[_] : Sync](resourcePoolRegistration: ResourcePoolRegistration, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.yarnRepository.complete(resourcePoolRegistration.id, instant)
        .transact(workspaceContext.context.transactor).void

    override def run[F[_] : Sync : Clock](resourcePoolRegistration: ResourcePoolRegistration, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.yarnClient.createPool(resourcePoolRegistration.name, resourcePoolRegistration.cores, resourcePoolRegistration.memory)

  }

  implicit object ResourcePoolRegistrationDeprovisioningTask extends DeprovisioningTask[ResourcePoolRegistration] {

    override def run[F[_] : Sync : Clock](resourcePoolRegistration: ResourcePoolRegistration, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.yarnClient.deletePool(resourcePoolRegistration.name)

  }

  implicit val provisionable: Provisionable[ResourcePoolRegistration] = Provisionable.deriveFromTasks

}
