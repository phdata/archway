package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppConfig

case class CreateDatabaseDirectory(location: String, onBehalfOf: Option[String])

object CreateDatabaseDirectory {
  implicit val show: Show[CreateDatabaseDirectory] =
    Show.show(c => s"creating db directory ${c.location}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateDatabaseDirectory] =
    ProvisionTask.instance[F, CreateDatabaseDirectory] {
      create =>
        Kleisli[F, AppConfig[F], ProvisionResult] { config =>
          F.map(F.attempt(config.hdfsClient.createDirectory(create.location, create.onBehalfOf))) {
            case Left(exception) => Error(exception)
            case Right(_) => Success[CreateDatabaseDirectory]
          }
        }
    }
}
