package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.Kafka
import doobie.implicits._
import org.apache.sentry.core.model.kafka.ConsumerGroup

case class GrantRoleToConsumerGroup(applicationId: Long, consumerGroup: String, roleName: String) {
  val consumerGroupInstance: ConsumerGroup = new ConsumerGroup(consumerGroup)
}

object GrantRoleToConsumerGroup {

  implicit val viewer: Show[GrantRoleToConsumerGroup] =
    Show.show(g => s"granting role ${g.roleName} rights to consumer group ${g.consumerGroup}")

  implicit object GrantRoleToConsumerGroupCompletionTask extends CompletionTask[GrantRoleToConsumerGroup] {

    override def apply[F[_] : Sync](grantRoleToConsumerGroup: GrantRoleToConsumerGroup, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.applicationRepository.consumerGroupAccess(grantRoleToConsumerGroup.applicationId, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object GrantRoleToConsumerGroupProvisioningTask extends ProvisioningTask[GrantRoleToConsumerGroup] {

    override def apply[F[_] : Sync : Clock](grantRoleToConsumerGroup: GrantRoleToConsumerGroup, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext
        .context
        .sentryClient
        .grantPrivilege(grantRoleToConsumerGroup.roleName,
          Kafka,
          s"ConsumerGroup=${grantRoleToConsumerGroup.consumerGroup}->action=ALL")

  }

  implicit val provisionable: Provisionable[GrantRoleToConsumerGroup] = Provisionable.deriveProvisionable

}
