package com.heimdali.tasks

import cats.Show
import cats.data._
import cats.effect._
import com.heimdali.models.AppConfig

case class CreateRole(name: String)

object CreateRole {

  implicit val show: Show[CreateRole] =
    Show.show(c => s"creating sentry role ${c.name}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateRole] =
    ProvisionTask.instance { create =>
        Kleisli[F, AppConfig[F], ProvisionResult] { config =>
          F.map(F.attempt(config.hiveClient.createRole(create.name))) {
            case Left(exception) => Error(exception)
            case Right(_) => Success[CreateRole]
          }
        }
    }

}
