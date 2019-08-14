package io.phdata.provisioning

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import io.phdata.clients.Kafka
import doobie.implicits._

object ConsumerGroupGrantProvisioning {

  implicit val viewer: Show[ConsumerGroupGrant] =
    Show.show(g => s"""role "${g.roleName}" access to consumer group "${g.consumerGroup}"""")

  implicit object GrantRoleToConsumerGroupProvisioningTask extends ProvisioningTask[ConsumerGroupGrant] {

    override def complete[F[_]: Sync](
        consumerGroupGrant: ConsumerGroupGrant,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.applicationRepository
        .consumerGroupAccess(consumerGroupGrant.applicationId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](
        consumerGroupGrant: ConsumerGroupGrant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.sentryClient.grantPrivilege(
        consumerGroupGrant.roleName,
        Kafka,
        s"ConsumerGroup=${consumerGroupGrant.consumerGroup}->action=ALL"
      )

  }

  implicit object GrantRoleToConsumerGroupDeprovisioningTask extends DeprovisioningTask[ConsumerGroupGrant] {

    override def run[F[_]: Sync: Clock](
        consumerGroupGrant: ConsumerGroupGrant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.sentryClient.removePrivilege(
        consumerGroupGrant.roleName,
        Kafka,
        s"ConsumerGroup=${consumerGroupGrant.consumerGroup}->action=ALL"
      )

  }

  implicit val provisionable: Provisionable[ConsumerGroupGrant] = Provisionable.deriveFromTasks

}
