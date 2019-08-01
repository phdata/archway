package io.phdata.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import io.phdata.clients.Kafka
import doobie.implicits._

case class KafkaTopicGrant(id: Long, name: String, sentryRole: String, actions: NonEmptyList[String])

object KafkaTopicGrant {

  implicit val show: Show[KafkaTopicGrant] =
    Show.show(s => s"""${s.actions.mkString_(",")} permissions to role "${s.sentryRole}" for topic "${s.name}"""")

  implicit object GrantTopicAccessProvisioningTask extends ProvisioningTask[KafkaTopicGrant] {

    override def complete[F[_]: Sync](
        kafkaTopicGrant: KafkaTopicGrant,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.topicGrantRepository
        .topicAccess(kafkaTopicGrant.id, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](
        kafkaTopicGrant: KafkaTopicGrant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      kafkaTopicGrant.actions
        .traverse[F, Unit] { action =>
          workspaceContext.context.sentryClient
            .grantPrivilege(kafkaTopicGrant.sentryRole, Kafka, s"Topic=${kafkaTopicGrant.name}->action=$action")
        }
        .map(_.combineAll)

  }

  implicit object GrantTopicAccessDeprovisioningTask extends DeprovisioningTask[KafkaTopicGrant] {

    override def run[F[_]: Sync: Clock](
        kafkaTopicGrant: KafkaTopicGrant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      kafkaTopicGrant.actions
        .traverse[F, Unit] { action =>
          workspaceContext.context.sentryClient
            .removePrivilege(kafkaTopicGrant.sentryRole, Kafka, s"Topic=${kafkaTopicGrant.name}->action=$action")
        }
        .map(_.combineAll)

  }

  implicit val provisionable: Provisionable[KafkaTopicGrant] = Provisionable.deriveFromTasks

}
