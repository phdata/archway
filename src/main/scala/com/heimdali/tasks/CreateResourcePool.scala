package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext

case class CreateResourcePool(name: String, cores: Int, memory: Int)

object CreateResourcePool {

  implicit val show: Show[CreateResourcePool] =
    Show.show(c => s"creating resource pool ${c.name} with ${c.cores} cores and ${c.memory} memory")

  implicit def provisioner[F[_]]: ProvisionTask[F, CreateResourcePool] = new ProvisionTask[F, CreateResourcePool] {
    override def provision(create: CreateResourcePool)(implicit F: Effect[F]): Kleisli[F, AppContext[F], ProvisionResult] =
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.map(F.attempt(config.yarnClient.createPool(create.name, create.cores, create.memory))) {
          case Left(exception) => Error(exception)
          case Right(_) => Success[CreateResourcePool]
        }
      }
  }

}
