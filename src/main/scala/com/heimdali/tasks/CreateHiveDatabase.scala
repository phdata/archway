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
    Show.show(s => s"""create(d) Hive database "${s.name}" at "${s.location}"""")

  implicit val provisioner: ProvisionTask[CreateHiveDatabase] = ???

  //    override val provision: Kleisli[IO, AppConfig, ProvisionResult] =
  //      Kleisli[IO, AppConfig, ProvisionResult] { config =>
  //          config.hiveClient.createDatabase(name, location).attempt.map {
  //            case Left(exception) => Error(s"${show()} due to \"$exception\"")
  //            case Right(_) => Success(show())
  //          }
  //      }

}