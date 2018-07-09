package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class RemoveMember(groupDN: String, userDN: String)

object RemoveMember {

  implicit val show: Show[RemoveMember] = ???

  implicit val provisioner: ProvisionTask[RemoveMember] = ???

}