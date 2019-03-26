package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data.Kleisli
import cats.effect.{Effect, Timer}
import cats.implicits._
import doobie.implicits._

case class CreateResourcePool(id: Long, name: String, cores: Int, memory: Int)

object CreateResourcePool {

  implicit val show: Show[CreateResourcePool] =
    Show.show(c => s"creating resource pool ${c.name} with ${c.cores} cores and ${c.memory} memory")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, CreateResourcePool] =
    ProvisionTask.instance { create =>
      Kleisli { config =>
        config
          .yarnClient
          .createPool(create.name, create.cores, create.memory)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(create, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- config
                  .yarnRepository
                  .complete(create.id, Instant.ofEpochMilli(time))
                  .transact(config.transactor)
              } yield Success(create)
          }
      }
    }

}
