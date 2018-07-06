package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class AllowDatabaseAccess(databaseName: String, roleName: String) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object AllowDatabaseAccess {
  def show: Show[AllowDatabaseAccess] = ???
}
