package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.Kafka
import doobie.implicits._

case class GrantTopicAccess(id: Long, name: String, sentryRole: String, actions: NonEmptyList[String])

object GrantTopicAccess {

  implicit val show: Show[GrantTopicAccess] =
    Show.show(s => s"granting ${s.actions.mkString_(",")} on ${s.name} to ${s.sentryRole}")

  implicit object GrantTopicAccessCompletionTask extends CompletionTask[GrantTopicAccess] {

    override def apply[F[_] : Sync](grantTopicAccess: GrantTopicAccess, instant: Instant, workspaceContext: WorkspaceContext[F]): F[Unit] =
      workspaceContext.context.topicGrantRepository.topicAccess(grantTopicAccess.id, instant)
        .transact(workspaceContext.context.transactor).void

  }

  implicit object GrantTopicAccessProvisioningTask extends ProvisioningTask[GrantTopicAccess] {

    override def apply[F[_] : Sync : Clock](grantTopicAccess: GrantTopicAccess, workspaceContext: WorkspaceContext[F]): F[Unit] =
      grantTopicAccess.actions.traverse[F, Unit] { action =>
        workspaceContext.context.sentryClient.grantPrivilege(grantTopicAccess.sentryRole, Kafka, s"Topic=${grantTopicAccess.name}->action=$action")
      }.map(_.combineAll)

  }

  implicit val provisionable: Provisionable[GrantTopicAccess] = Provisionable.deriveProvisionable

}
