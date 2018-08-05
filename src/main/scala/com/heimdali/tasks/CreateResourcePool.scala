package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import doobie.implicits._

case class CreateResourcePool(id: Long, name: String, cores: Int, memory: Int)

object CreateResourcePool {

  implicit val show: Show[CreateResourcePool] =
    Show.show(c => s"creating resource pool ${c.name} with ${c.cores} cores and ${c.memory} memory")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateResourcePool] =
    ProvisionTask.instance { create =>
      Kleisli { config =>
        F.flatMap(F.attempt(config.yarnClient.createPool(create.name, create.cores, create.memory))) {
          case Left(exception) => F.pure(Error(create, exception))
          case Right(_) =>
            F.map(config
              .yarnRepository
              .complete(create.id)
              .transact(config.transactor)) { _ => Success(create) }
        }
      }
    }

}
