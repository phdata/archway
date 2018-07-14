package com.heimdali.tasks

import cats._
import cats.data._
import cats.effect.Effect
import cats.implicits._
import com.heimdali.models.AppContext

case class CreateHiveDatabase(name: String, location: String)

object CreateHiveDatabase {

  implicit val viewer: Show[CreateHiveDatabase] =
    Show.show(c => s"""creating Hive database "${c.name}" at "${c.location}"""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, CreateHiveDatabase] =
    ProvisionTask.instance { create =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        config.hiveClient.createDatabase(create.name, create.location).attempt.map {
          case Left(exception) => Error[CreateHiveDatabase](exception)
          case Right(_) => Success[CreateHiveDatabase]
        }
      }
    }

}
