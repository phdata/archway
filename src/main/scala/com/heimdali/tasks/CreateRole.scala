package com.heimdali.tasks

import cats.Show
import cats.data._
import cats.effect._
import com.heimdali.models.AppConfig

case class CreateRole(name: String)

object CreateRole {

  implicit val show: Show[CreateRole] =
    Show.show(c => s"creating sentry role ${c.name}")

  implicit val provisioner: ProvisionTask[CreateRole] =
      create => Kleisli[IO, AppConfig, ProvisionResult] { config =>
        config.hiveClient.createRole(create.name).attempt.map {
          case Left(exception) => Error(exception)
          case Right(_) => Success[CreateRole]
        }
      }

}
