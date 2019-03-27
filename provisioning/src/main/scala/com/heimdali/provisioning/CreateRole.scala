package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import doobie.implicits._

case class CreateRole(id: Long, name: String)

object CreateRole {

  implicit val show: Show[CreateRole] =
    Show.show(c => s"creating sentry role ${c.name}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, CreateRole] =
    ProvisionTask.instance { create =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .sentryClient
          .createRole(create.name)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, create, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .ldapRepository
                  .roleCreated(create.id, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, create)
          }
      }
    }

}
