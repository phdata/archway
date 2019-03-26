package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.effect.{Effect, Timer}
import cats.implicits._
import com.heimdali.AppContext
import doobie.implicits._

import scala.concurrent.duration._

case class CreateHiveDatabase(workspaceId: Long, name: String, location: String)

object CreateHiveDatabase {

  implicit val viewer: Show[CreateHiveDatabase] =
    Show.show(c => s"""creating Hive database "${c.name}" at "${c.location}"""")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, CreateHiveDatabase] =
    ProvisionTask.instance { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config.hiveClient.createDatabase(create.name, create.location).attempt.flatMap {
          case Left(exception) => Effect[F].pure(Error(create, exception))
          case Right(_) =>
            for {
              time <- Timer[F].clock.realTime(MILLISECONDS)
              _ <- config
                .databaseRepository
                .databaseCreated(create.workspaceId, Instant.ofEpochMilli(time))
                .transact(config.transactor)
            } yield Success(create)
        }
      }
    }

}
