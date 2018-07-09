package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig
import org.apache.hadoop.fs.Path

case class GrantLocationAccess(role: String, location: String)

object GrantLocationAccess {

  implicit val show: Show[GrantLocationAccess] = ???

  implicit val provisioner: ProvisionTask[GrantLocationAccess] = ???

}