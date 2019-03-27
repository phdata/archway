package com.heimdali.provisioning

import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.AppContext
import com.typesafe.scalalogging.LazyLogging

trait ProvisionTask[F[_], A] {

  def provision(provisionable: A)(implicit F: Effect[F]): Kleisli[F, WorkspaceContext[F], ProvisionResult]

}

object ProvisionTask extends LazyLogging {

  def apply[F[_], A](implicit ev: ProvisionTask[F, A]): ProvisionTask[F, A] = ev

  implicit class ProvisionerOps[A](a: A) {

    def provision[F[_]](implicit provisioner: ProvisionTask[F, A], F: Effect[F]): Kleisli[F, WorkspaceContext[F], ProvisionResult] =
      ProvisionTask[F, A].provision(a)

  }

  def instance[F[_], A](kleisli: A => Kleisli[F, WorkspaceContext[F], ProvisionResult]) = new ProvisionTask[F, A] {

    override def provision(provisionable: A)(implicit F: Effect[F]): Kleisli[F, WorkspaceContext[F], ProvisionResult] = {
      val startLogging: Kleisli[F, WorkspaceContext[F], WorkspaceContext[F]] =
        Kleisli[F, WorkspaceContext[F], WorkspaceContext[F]] { case (id, context) =>
          F.delay {
            logger.debug(">>>> workspace: {}, class: {} ----", provisionable.getClass.getSimpleName, id)
            logger.debug("---- STATE: {}", provisionable)
            (id, context)
          }
        }

      val endLogging: Kleisli[F, ProvisionResult, ProvisionResult] =
        Kleisli[F, ProvisionResult, ProvisionResult] { result =>
          F.delay {
            result.messages.map(m =>
              m match {
                case SimpleMessage(m.workspaceId, m.message) => logger.debug("---- RESULT: {}", m)
                case ExceptionMessage(_, m, t) => logger.error(m, t)
              }
            )
            logger.debug("<<<< {} ----", provisionable.getClass.getSimpleName)
            result
          }
        }

      startLogging andThen kleisli(provisionable) andThen endLogging
    }
  }

}
