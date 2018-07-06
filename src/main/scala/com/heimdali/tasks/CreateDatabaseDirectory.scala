package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateDatabaseDirectory() extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object CreateDatabaseDirectory {
  def show: Show[CreateDatabaseDirectory] = ???
}
