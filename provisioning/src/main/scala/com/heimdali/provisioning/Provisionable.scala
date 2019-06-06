package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data.{NonEmptyList, WriterT}
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import simulacrum.typeclass

@typeclass
trait Provisionable[A] {
  def provision[F[_] : Clock : Sync](resource: A, workspaceContext: WorkspaceContext[F])(implicit show: Show[A]): WriterT[F, NonEmptyList[Message], ProvisionResult]
}

trait ProvisioningTask[A] {
  def apply[F[_] : Sync : Clock](a: A, workspaceContext: WorkspaceContext[F]): F[Unit]
}

trait CompletionTask[A] {
  def apply[F[_] : Sync](a: A, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit]
}

object Provisionable {

  def deriveProvisionable[A](implicit run: ProvisioningTask[A], complete: CompletionTask[A]): Provisionable[A] =
    new Provisionable[A] with LazyLogging {

      override def provision[F[_] : Clock : Sync](resource: A, workspaceContext: WorkspaceContext[F])(implicit show: Show[A]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
        WriterT(
          run(resource, workspaceContext).attempt.flatMap {
            case Left(exception: Exception) =>
              val messages = Error.message(resource, workspaceContext.workspaceId, exception)
              logger.error(messages.head.message, exception)
              (messages, Error.asInstanceOf[ProvisionResult]).pure[F]
            case Right(_) =>
              for {
                time <- Clock[F].realTime(scala.concurrent.duration.MILLISECONDS)
                instant = Instant.ofEpochMilli(time)
                _ <- complete(resource, instant, workspaceContext)
                messages = Success.message(resource, workspaceContext.workspaceId)
                _ <- logger.info(messages.head.message).pure[F]
              } yield (messages, Success.asInstanceOf[ProvisionResult])
          }
        )
    }

}