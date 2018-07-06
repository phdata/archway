package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateLDAPGroup(distinguishedName: String) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object CreateLDAPGroup {
  def show: Show[CreateLDAPGroup] = ???
}