package com.heimdali.tasks

import cats.Show
import cats.data._
import cats.effect._
import com.heimdali.models.AppConfig

case class CreateRole(name: String)

object CreateRole {

  implicit val show: Show[CreateRole] = ???

  implicit val provisioner: ProvisionTask[CreateRole] =
      createRole => Kleisli[IO, AppConfig, ProvisionResult] { config =>
        IO(config.sentryService.createRole("heimdali_api", createRole.name)).attempt.map {
          case Left(exception) => Error[CreateRole](exception)
          case Right(_) => Success("")
        }
      }

}