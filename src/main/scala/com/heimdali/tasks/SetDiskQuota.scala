package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class SetDiskQuota(location: String, sizeInGB: Int)

object SetDiskQuota {

  implicit val show: Show[SetDiskQuota] = ???

  implicit val provisioner: ProvisionTask[SetDiskQuota] = ???

}