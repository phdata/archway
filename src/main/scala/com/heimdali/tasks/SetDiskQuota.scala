package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class SetDiskQuota(location: String, sizeInGB: Int) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object SetDiskQuota {
  def show: Show[SetDiskQuota] = ???
}