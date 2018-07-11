package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateResourcePool(name: String, cores: Int, memory: Int)

object CreateResourcePool {

  implicit val show: Show[CreateResourcePool] =
    Show.show(c => s"creating resource pool ${c.name} with ${c.cores} cores and ${c.memory} memory")

  implicit val provisioner: ProvisionTask[CreateResourcePool] =
    create => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config.yarnClient.createPool(create.name, create.cores, create.memory).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[CreateResourcePool]
      }
    }

}
