package com.heimdali.tasks

import cats.Show
import cats.data._
import cats.effect._
import doobie.implicits._
import com.heimdali.models.AppContext

case class CreateRole(id: Long, name: String)

object CreateRole {

  implicit val show: Show[CreateRole] =
    Show.show(c => s"creating sentry role ${c.name}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateRole] =
    ProvisionTask.instance { create =>
        Kleisli[F, AppContext[F], ProvisionResult] { config =>
          F.flatMap(F.attempt(config.hiveClient.createRole(create.name))) {
            case Left(exception) => F.pure(Error(exception))
            case Right(_) =>
              F.map(config
                .ldapRepository
                .roleCreated(create.id)
                .transact(config.transactor)) { _ => Success[CreateRole] }
          }
        }
    }

}
