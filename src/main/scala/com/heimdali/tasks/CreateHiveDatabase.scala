package com.heimdali.tasks

import cats.data.Kleisli
import cats.effect.IO
import cats.syntax.show
import com.heimdali.models.AppConfig

case class CreateHiveDatabase(name: String, location: String) extends ProvisionTask {

    override val provision: Kleisli[IO, AppConfig, ProvisionResult] =
      Kleisli[IO, AppConfig, ProvisionResult] { config =>
          config.hiveClient.createDatabase(name, location).attempt.map {
            case Left(exception) => Error(s"${show()} due to \"$exception\"")
            case Right(_) => Success(show())
          }
      }

}

object CreateHiveDatabase {

  implicit val viewer: Show[CreateHiveDatabase] = Show.show(s => s"create(d) Hive database \"${s.name}\" at \"${s.location}\"")

}