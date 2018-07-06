package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import cats.syntax.show
import com.heimdali.models.AppConfig

case class AddMember(groupDN: String, userDN: String) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object AddMember {
  def show: Show[AddMember] = Show.show(am => s"adding \"${am.userDN}\" to \"${am.groupDN}")
}