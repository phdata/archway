package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateKafkaTopic(name: String, partitions: Int, replicationFactor: Int)

object CreateKafkaTopic {

  implicit val show: Show[CreateKafkaTopic] =
    Show.show(c => s"creating kafka topic ${c.name} with ${c.partitions} partitions and a replication factor of ${c.replicationFactor}")

  implicit val provision: ProvisionTask[CreateKafkaTopic] =
    create => Kleisli[IO, AppConfig, ProvisionResult[CreateKafkaTopic]] { config =>
      config.kafkaClient.createTopic(create.name, create.partitions, create.replicationFactor).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[CreateKafkaTopic]
      }
    }

}