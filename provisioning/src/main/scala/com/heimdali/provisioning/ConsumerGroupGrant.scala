package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.Kafka
import doobie.implicits._
import org.apache.sentry.core.model.kafka.ConsumerGroup

case class ConsumerGroupGrant(applicationId: Long, consumerGroup: String, roleName: String) {
  val consumerGroupInstance: ConsumerGroup = new ConsumerGroup(consumerGroup)
}

object ConsumerGroupGrant {

  implicit val viewer: Show[ConsumerGroupGrant] =
    Show.show(g => s"""role "${g.roleName}" access to consumer group "${g.consumerGroup}"""")

  implicit object GrantRoleToConsumerGroupProvisioningTask extends ProvisioningTask[ConsumerGroupGrant] {

    override def complete[F[_] : Sync](consumerGroupGrant: ConsumerGroupGrant, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.applicationRepository.consumerGroupAccess(consumerGroupGrant.applicationId, instant)
        .transact(workspaceContext.context.transactor).void

    override def run[F[_] : Sync : Clock](consumerGroupGrant: ConsumerGroupGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext
        .context
        .sentryClient
        .grantPrivilege(consumerGroupGrant.roleName,
          Kafka,
          s"ConsumerGroup=${consumerGroupGrant.consumerGroup}->action=ALL")

  }

  implicit object GrantRoleToConsumerGroupDeprovisioningTask extends DeprovisioningTask[ConsumerGroupGrant] {

    override def run[F[_] : Sync : Clock](consumerGroupGrant: ConsumerGroupGrant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext
        .context
        .sentryClient
        .removePrivilege(consumerGroupGrant.roleName,
          Kafka,
          s"ConsumerGroup=${consumerGroupGrant.consumerGroup}->action=ALL")

  }

  implicit val provisionable: Provisionable[ConsumerGroupGrant] = Provisionable.deriveFromTasks

}
