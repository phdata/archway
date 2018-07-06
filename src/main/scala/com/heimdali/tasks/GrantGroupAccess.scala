package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class GrantGroupAccess(role: String, groupName: String) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object GrantGroupAccess {
  def show: Show[GrantGroupAccess] = ???
}