package com.heimdali.services

import cats.effect.Effect
import com.heimdali.clients.KafkaClient
import com.heimdali.models.{AppContext, KafkaTopic}
import com.heimdali.tasks.ProvisionTask._

trait KafkaService[F[_]] {

  def create(workspaceId: Long, kafkaTopic: KafkaTopic): F[KafkaTopic]

}

class KafkaServiceImpl[F[_] : Effect](appContext: AppContext[F]) extends KafkaService[F] {
  override def create(workspaceId: Long, kafkaTopic: KafkaTopic): F[KafkaTopic] =
    for {
      id <- appContext
        .kafkaRepository
        .create(workspaceId, kafkaTopic)
        .transact(appContext.transactor)

      _ <- kafkaTopic.provision.run(appContext)

      updated <- appContext
        .kafkaReposiory
        .find(id)
    } yield updated
}