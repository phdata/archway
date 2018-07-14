package com.heimdali.tasks

import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext

trait ProvisionTask[F[_], A] {

  def provision(provisionable: A)(implicit F: Effect[F]): Kleisli[F, AppContext[F], ProvisionResult]

}

object ProvisionTask {

  def apply[F[_], A](implicit ev: ProvisionTask[F, A]): ProvisionTask[F, A] = ev

  implicit class ProvisionerOps[A](a: A) {

    def provision[F[_]](implicit provisioner: ProvisionTask[F, A], F: Effect[F]) =
      ProvisionTask[F, A].provision(a)

  }

  def instance[F[_] : Effect, A](kleisli: A => Kleisli[F, AppContext[F], ProvisionResult]) = new ProvisionTask[F, A] {
    override def provision(provisionable: A)(implicit F: Effect[F]): Kleisli[F, AppContext[F], ProvisionResult] =
      kleisli(provisionable)
  }

}
