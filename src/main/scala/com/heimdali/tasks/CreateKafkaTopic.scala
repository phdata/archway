package com.heimdali.tasks

import cats._
import cats.data._
import cats.implicits._
import cats.effect.Effect
import com.heimdali.models.AppContext
import doobie.implicits._

case class CreateKafkaTopic(id: Long, name: String, partitions: Int, replicationFactor: Int)

object CreateKafkaTopic {

  implicit val show: Show[CreateKafkaTopic] =
    Show.show(c => s"creating kafka topic ${c.name} with ${c.partitions} partitions and a replication factor of ${c.replicationFactor}")

  implicit def provision[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateKafkaTopic] =
    ProvisionTask.instance { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config
          .kafkaClient
          .createTopic(create.name, create.partitions, create.replicationFactor)
          .attempt
          .flatMap {
            case Left(exception) => F.pure(Error(create, exception))
            case Right(_) =>
              config
                .kafkaRepository
                .topicCreated(create.id)
                .transact(config.transactor)
                .map { _ => Success(create) }
          }
      }
    }

}
