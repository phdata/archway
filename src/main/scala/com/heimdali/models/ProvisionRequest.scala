package com.heimdali.models

import java.time.Instant

import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.tasks.ProvisionResult
import doobie._
import doobie.implicits._

case class ProvisionAttempt(id: Long, started: Instant, finished: Option[Instant])

object ProvisionRequest {

  def provision(workspaceRequestId: Int): Kleisli[IO, AppConfig, Unit] =
    Kleisli[IO, AppConfig, Unit] { config =>
      for {
        attempt <- config.provisionAttemptRepository.create(workspaceRequestId).transact(config.transactor)
      } yield ()
    }

}