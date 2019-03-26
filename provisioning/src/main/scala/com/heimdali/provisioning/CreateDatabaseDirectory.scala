package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import com.heimdali.AppContext
import scala.concurrent.duration._
import doobie.implicits._

case class CreateDatabaseDirectory(workspaceId: Long, location: String, onBehalfOf: Option[String])

object CreateDatabaseDirectory {
  implicit val show: Show[CreateDatabaseDirectory] =
    Show.show(c => s"creating db directory ${c.location}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, CreateDatabaseDirectory] =
    ProvisionTask.instance[F, CreateDatabaseDirectory] { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config
          .hdfsClient
          .createDirectory(create.location, create.onBehalfOf)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(create, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(MILLISECONDS)
                _ <- config
                  .databaseRepository
                  .directoryCreated(create.workspaceId, Instant.ofEpochMilli(time))
                  .transact[F](config.transactor)
              } yield Success(create)
          }
      }
    }
}
