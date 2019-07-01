package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class KafkaTopicRegistration(id: Long, name: String, partitions: Int, replicationFactor: Int)

object KafkaTopicRegistration {

  implicit val show: Show[KafkaTopicRegistration] =
    Show.show(k => s"""topic "${k.name}" (p: ${k.partitions}, rf: ${k.replicationFactor}""")

  implicit object KafkaTopicRegistrationProvisioningTask extends ProvisioningTask[KafkaTopicRegistration] {

    override def complete[F[_]: Sync](
        kafkaTopicRegistration: KafkaTopicRegistration,
        instant: Instant,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.kafkaRepository
        .topicCreated(workspaceContext.workspaceId, instant)
        .transact(workspaceContext.context.transactor)
        .void

    override def run[F[_]: Sync: Clock](
        kafkaTopicRegistration: KafkaTopicRegistration,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.kafkaClient.createTopic(
        kafkaTopicRegistration.name,
        kafkaTopicRegistration.partitions,
        kafkaTopicRegistration.replicationFactor
      )

  }

  implicit object KafkaTopicRegistrationDeprovisioningTask extends DeprovisioningTask[KafkaTopicRegistration] {

    override def run[F[_]: Sync: Clock](
        kafkaTopicRegistration: KafkaTopicRegistration,
        workspaceContext: WorkspaceContext[F]
    ): F[Unit] =
      workspaceContext.context.kafkaClient.deleteTopic(kafkaTopicRegistration.name)

  }

  implicit val provisionable: Provisionable[KafkaTopicRegistration] = Provisionable.deriveFromTasks

}
