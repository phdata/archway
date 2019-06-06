package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.effect.{Clock, Sync}
import cats.implicits._
import doobie.implicits._

case class CreateKafkaTopic(id: Long, name: String, partitions: Int, replicationFactor: Int)

object CreateKafkaTopic {

  implicit val show: Show[CreateKafkaTopic] =
    Show.show(c => s"creating kafka topic ${c.name} with ${c.partitions} partitions and a replication factor of ${c.replicationFactor}")

  implicit object CreateKafkaTopicCompletionTask extends CompletionTask[CreateKafkaTopic] {

    override def apply[F[_] : Sync](a: CreateKafkaTopic, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.kafkaRepository.topicCreated(workspaceContext.workspaceId, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object CreateKafkaTopicProvisioningTask extends ProvisioningTask[CreateKafkaTopic] {

    override def apply[F[_] : Sync : Clock](createKafkaTopic: CreateKafkaTopic, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext
        .context
        .kafkaClient
        .createTopic(createKafkaTopic.name, createKafkaTopic.partitions, createKafkaTopic.replicationFactor)

  }

  implicit val provisionable: Provisionable[CreateKafkaTopic] = Provisionable.deriveProvisionable

}
