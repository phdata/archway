package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext

case class CreateKafkaTopic(name: String, partitions: Int, replicationFactor: Int)

object CreateKafkaTopic {

  implicit val show: Show[CreateKafkaTopic] =
    Show.show(c => s"creating kafka topic ${c.name} with ${c.partitions} partitions and a replication factor of ${c.replicationFactor}")

  implicit def provision[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateKafkaTopic] =
    ProvisionTask.instance { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.map(F.attempt(config.kafkaClient.createTopic(create.name, create.partitions, create.replicationFactor))) {
          case Left(exception) => Error(exception)
          case Right(_) => Success[CreateKafkaTopic]
        }
      }
    }

}
