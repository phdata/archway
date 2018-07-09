package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class CreateResourcePool(name: String, cores: Int, memory: Int)

object CreateResourcePool {

  implicit val show: Show[CreateResourcePool] = ???

  implicit val provisioner: ProvisionTask[CreateResourcePool] = ???

}