package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.implicits._
import cats.effect.{Effect, Timer}
import com.heimdali.AppContext
import doobie.implicits._

case class CreateKafkaTopic(id: Long, name: String, partitions: Int, replicationFactor: Int)

object CreateKafkaTopic {

  implicit val show: Show[CreateKafkaTopic] =
    Show.show(c => s"creating kafka topic ${c.name} with ${c.partitions} partitions and a replication factor of ${c.replicationFactor}")

  implicit def provision[F[_] : Effect : Timer]: ProvisionTask[F, CreateKafkaTopic] =
    ProvisionTask.instance { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config
          .kafkaClient
          .createTopic(create.name, create.partitions, create.replicationFactor)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(create, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- config
                  .kafkaRepository
                  .topicCreated(create.id, Instant.ofEpochMilli(time))
                  .transact(config.transactor)
              } yield Success(create)
          }
      }
    }

}
