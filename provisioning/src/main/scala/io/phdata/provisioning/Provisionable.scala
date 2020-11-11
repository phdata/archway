package io.phdata.provisioning

import java.time.Instant

import cats.Show
import cats.data.{NonEmptyList, WriterT}
import cats.effect._
import cats.implicits._
import io.phdata.config.AppConfig
import com.typesafe.scalalogging.LazyLogging

trait Provisionable[A] {

  def provision[F[_]: Clock: Sync](resource: A, workspaceContext: WorkspaceContext[F])(
      implicit show: Show[A]
  ): WriterT[F, NonEmptyList[Message], ProvisionResult]

  def deprovision[F[_]: Clock: Sync](resource: A, workspaceContext: WorkspaceContext[F])(
      implicit show: Show[A]
  ): WriterT[F, NonEmptyList[Message], ProvisionResult]

}

object Provisionable {

  def deriveFromSteps[A](resources: (A, AppConfig) => NonEmptyList[TypeWith[Provisionable]]): Provisionable[A] =
    new Provisionable[A] {

      private def provisionOne[F[_]: Clock: Sync](
          workspaceContext: WorkspaceContext[F]
      )(typewith: TypeWith[Provisionable]): WriterT[F, NonEmptyList[Message], ProvisionResult] = {
        import typewith._
        typewith.evidence.provision(typewith.value, workspaceContext)
      }

      private def deprovisionOne[F[_]: Clock: Sync](
          workspaceContext: WorkspaceContext[F]
      )(typewith: TypeWith[Provisionable]): WriterT[F, NonEmptyList[Message], ProvisionResult] = {
        import typewith._
        typewith.evidence.deprovision(typewith.value, workspaceContext)
      }

      override def provision[F[_]: Clock: Sync](resource: A, workspaceContext: WorkspaceContext[F])(
          implicit show: Show[A]
      ): WriterT[F, NonEmptyList[Message], ProvisionResult] =
        resources(resource, workspaceContext.context.appConfig)
          .nonEmptyTraverse(provisionOne(workspaceContext))
          .map(_.reduce)

      override def deprovision[F[_]: Clock: Sync](resource: A, workspaceContext: WorkspaceContext[F])(
          implicit show: Show[A]
      ): WriterT[F, NonEmptyList[Message], ProvisionResult] =
        resources(resource, workspaceContext.context.appConfig)
          .nonEmptyTraverse(deprovisionOne(workspaceContext)(_))
          .map(_.reduce)
    }

  def deriveFromTasks[A](
      provisioningTask: ProvisioningTask[A],
      deprovisioningTask: DeprovisioningTask[A]
  ): Provisionable[A] =
    new Provisionable[A] with LazyLogging {

      override def deprovision[F[_]: Clock: Sync](resource: A, workspaceContext: WorkspaceContext[F])(
          implicit show: Show[A]
      ): WriterT[F, NonEmptyList[Message], ProvisionResult] =
        WriterT {
          logger.info("DEPROVISIONING STARTING: {}", resource)
          deprovisioningTask.run(resource, workspaceContext).attempt.map {
            case Left(exception: Exception) =>
              val messages = Error.message(resource, workspaceContext.workspaceId, exception)
              logger.info("DEPROVISIONING FAILED!!: {}", resource)
              logger.error(messages.head.message, exception)
              (messages, Error.asInstanceOf[ProvisionResult])
            case Right(_) =>
              val messages = Success.message(resource, workspaceContext.workspaceId)
              logger.info(messages.head.message)
              logger.info("DEPROVISIONING FINISHED: {}", resource)
              (messages, Success.asInstanceOf[ProvisionResult])
          }
        }

      override def provision[F[_]: Clock: Sync](resource: A, workspaceContext: WorkspaceContext[F])(
          implicit show: Show[A]
      ): WriterT[F, NonEmptyList[Message], ProvisionResult] =
        WriterT(
          for {
            _ <- Sync[F].delay(logger.info("PROVISIONING STARTING: {}", resource))
            maybeResult <- provisioningTask.run(resource, workspaceContext).attempt
            result <- maybeResult match {
              case Left(exception: Exception) =>
                val messages = Error.message(resource, workspaceContext.workspaceId, exception)
                logger.error(messages.head.message, exception)
                Sync[F].delay((messages, Error.asInstanceOf[ProvisionResult]))
              case Right(_) =>
                for {
                  time <- Clock[F].realTime(scala.concurrent.duration.MILLISECONDS)
                  instant = Instant.ofEpochMilli(time)
                  _ <- provisioningTask.complete(resource, instant, workspaceContext)
                  messages = Success.message(resource, workspaceContext.workspaceId)
                  _ <- Sync[F].delay(logger.info(messages.head.message))
                } yield (messages, Success.asInstanceOf[ProvisionResult])
            }
            _ <- Sync[F].delay(logger.info("PROVISIONING FINISHED: {}", resource))
          } yield result
        )

    }
}
