package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext
import doobie.implicits._

case class CreateDatabaseDirectory(workspaceId: Long, location: String, onBehalfOf: Option[String])

object CreateDatabaseDirectory {
  implicit val show: Show[CreateDatabaseDirectory] =
    Show.show(c => s"creating db directory ${c.location}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateDatabaseDirectory] =
    ProvisionTask.instance[F, CreateDatabaseDirectory] {
      create =>
        Kleisli[F, AppContext[F], ProvisionResult] { config =>
          F.flatMap(F.attempt(config.hdfsClient.createDirectory(create.location, create.onBehalfOf))) {
            case Left(exception) => F.pure(Error(exception))
            case Right(_) =>
              F.map(config
                .databaseRepository
                .directoryCreated(create.workspaceId)
                .transact(config.transactor)) { _ => Success[CreateDatabaseDirectory] }
          }
        }
    }
}
