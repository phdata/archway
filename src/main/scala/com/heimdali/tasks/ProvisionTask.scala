package com.heimdali.tasks

import cats.data.Kleisli
import cats.implicits._
import cats.effect.IO
import com.heimdali.models.AppConfig

trait ProvisionTask {

  def provision: Kleisli[IO, AppConfig, ProvisionResult]

  def ~(provisionTask: ProvisionTask): Kleisli[IO, AppConfig, ProvisionResult] = ???

}