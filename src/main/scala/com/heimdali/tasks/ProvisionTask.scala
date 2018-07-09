package com.heimdali.tasks

import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

trait ProvisionTask[A] {

  def provision(provisionable: A): Kleisli[IO, AppConfig, ProvisionResult]

}

object ProvisionTask {

  def apply[A](implicit ev: ProvisionTask[A]): ProvisionTask[A] = ev

  implicit class ProvisionerOps[A](a: A) {
    def provision(implicit provisioner: ProvisionTask[A]) = {
      ProvisionTask[A].provision(a)
    }
  }

}