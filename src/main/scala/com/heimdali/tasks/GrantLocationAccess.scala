package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig
import org.apache.hadoop.fs.Path

case class GrantLocationAccess(role: String, location: Path) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object GrantLocationAccess {
  def show: Show[GrantLocationAccess] = ???
}