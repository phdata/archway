package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateDatabaseDirectory(location: String, onBehalfOf: Option[String])

object CreateDatabaseDirectory {
  implicit val show: Show[CreateDatabaseDirectory] =
    Show.show(c => s"creating db directory ${c.location}")

  implicit val provisioner: ProvisionTask[CreateDatabaseDirectory] =
    create => Kleisli[IO, AppConfig, ProvisionResult[CreateDatabaseDirectory]] { config =>
      config.hdfsClient.createDirectory(create.location, create.onBehalfOf).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[CreateDatabaseDirectory]
      }
    }
}
