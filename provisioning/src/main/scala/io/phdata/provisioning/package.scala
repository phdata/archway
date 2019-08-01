package io.phdata

import java.time.Instant

import cats.effect.{Clock, Sync}

package object provisioning
    extends LDAPRegistrationProvisioning with HiveProvisioning with ApplicationProvisioning with KafkaProvisioning
    with YarnProvisioning with WorkspaceRequestProvisioning {

  case class WorkspaceContext[F[_]](workspaceId: Long, context: AppContext[F])

  trait Task[A] {
    def run[F[_]: Sync: Clock](a: A, workspaceContext: WorkspaceContext[F]): F[Unit]
  }

  trait ProvisioningTask[A] extends Task[A] with CompletionTask[A]

  trait DeprovisioningTask[A] extends Task[A]

  trait CompletionTask[A] {
    def complete[F[_]: Sync](a: A, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit]
  }

}
