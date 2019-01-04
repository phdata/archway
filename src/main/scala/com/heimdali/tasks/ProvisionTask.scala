package com.heimdali.tasks

import cats.implicits._
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext
import com.typesafe.scalalogging.LazyLogging
import doobie.free.connection.ConnectionIO

trait ProvisionTask[F[_], A] {

  def provision(provisionable: A)(implicit F: Effect[F]): Kleisli[F, AppContext[F], ProvisionResult]

}

object ProvisionTask extends LazyLogging {

  def apply[F[_], A](implicit ev: ProvisionTask[F, A]): ProvisionTask[F, A] = ev

  implicit class ProvisionerOps[A](a: A) {

    def provision[F[_]](implicit provisioner: ProvisionTask[F, A], F: Effect[F]) =
      ProvisionTask[F, A].provision(a)

  }


  def instance[F[_], A](kleisli: A => Kleisli[F, AppContext[F], ProvisionResult]) = new ProvisionTask[F, A] {

    override def provision(provisionable: A)(implicit F: Effect[F]): Kleisli[F, AppContext[F], ProvisionResult] = {
      val startLogging: Kleisli[F, AppContext[F], AppContext[F]] =
        Kleisli[F, AppContext[F], AppContext[F]] { context =>
          F.delay {
            logger.debug(">>>> {} ----", provisionable.getClass.getSimpleName)
            logger.debug("---- STATE: {}", provisionable)
            context
          }
        }

      val endLogging: Kleisli[F, ProvisionResult, ProvisionResult] =
        Kleisli[F, ProvisionResult, ProvisionResult] { result =>
          F.delay {
            result.messages.map(m => logger.debug("---- RESULT: {}", m))
            logger.debug("<<<< {} ----", provisionable.getClass.getSimpleName)
            result
          }
        }

      startLogging andThen kleisli(provisionable) andThen endLogging
    }
  }

}
