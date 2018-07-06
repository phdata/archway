package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateResourcePool(name: String, cores: Int, memory: Int) extends ProvisionTask {
  override def provision: Kleisli[IO, AppConfig, ProvisionResult] = ???
}

object CreateResourcePool {
  def show: Show[CreateResourcePool] = ???
}