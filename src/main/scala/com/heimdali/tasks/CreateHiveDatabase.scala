package com.heimdali.tasks

import cats.effect.IO
import cats.implicits._
import cats._
import cats.data._
import cats.syntax.show
import com.heimdali.models.AppConfig

case class CreateHiveDatabase(name: String, location: String)

object CreateHiveDatabase {

  implicit val viewer: Show[CreateHiveDatabase] =
    Show.show(c => s"""creating Hive database "${c.name}" at "${c.location}"""")

  implicit val provisioner: ProvisionTask[CreateHiveDatabase] =
        create => Kleisli[IO, AppConfig, ProvisionResult] { config =>
            config.hiveClient.createDatabase(create.name, create.location).attempt.map {
              case Left(exception) => Error[CreateHiveDatabase](exception)
              case Right(_) => Success[CreateHiveDatabase]
            }
        }

}
