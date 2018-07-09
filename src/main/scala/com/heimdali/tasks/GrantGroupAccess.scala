package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class GrantGroupAccess(role: String, groupName: String)

object GrantGroupAccess {

  implicit val show: Show[GrantGroupAccess] = ???

  implicit val provisioner: ProvisionTask[GrantGroupAccess] = ???

}