package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateDatabaseDirectory(location: String)

object CreateDatabaseDirectory {
  implicit val show: Show[CreateDatabaseDirectory] = ???

  implicit val provisioner: ProvisionTask[CreateDatabaseDirectory] = ???
}
