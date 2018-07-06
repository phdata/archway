package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class GrantDatabaseAccess(role: String, databaseName: String) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object GrantDatabaseAccess {
  def show: Show[GrantDatabaseAccess] = ???
}