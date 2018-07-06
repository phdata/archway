package com.heimdali.tasks

import cats._
import cats.data._
import cats.effect._
import com.heimdali.models.AppConfig

case class CreateRole(name: String) extends ProvisionTask {

  override val provision: Kleisli[EitherT[IO, String, ?], AppConfig, Unit] =
      Kleisli[EitherT[IO, String, ?], AppConfig, Unit] { config =>
        EitherT.liftF(IO(config.sentryService.createRole("heimdali_api", name)).attempt.map {
          case Left(exception) => EitherT.left(exception.getMessage)
          case Right(_) => EitherT.rightT(())
        })
      }
  }

}