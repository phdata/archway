package com.heimdali.tasks

import cats._
import cats.data._
import cats.implicits._
import cats.effect._
import doobie.implicits._
import org.apache.sentry.provider.db.generic.tools.KafkaTSentryPrivilegeConverter

case class GrantTopicAccess(id: Long, name: String, sentryRole: String, actions: NonEmptyList[String]) {
  val permission = new KafkaTSentryPrivilegeConverter("kafka", "kafka")
}

object GrantTopicAccess {

  implicit val show: Show[GrantTopicAccess] =
    Show.show(s => "")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantTopicAccess] =
    ProvisionTask.instance(grant =>
      Kleisli { context =>
        grant.actions.traverse[F, Either[Throwable, Unit]] { action =>
          F.delay(
            context
              .sentryClient
              .grantPrivilege(
                "heimdali_api",
                grant.sentryRole,
                "kafka",
                grant.permission.fromString(s"Topic=${grant.name}->action=$action"))).attempt
        }
          .map(_.combineAll)
          .flatMap {
            case Left(exception) => F.pure(Error[GrantTopicAccess](exception))
            case Right(_) =>
              context
                .topicGrantRepository
                .topicAccess(grant.id)
                .transact(context.transactor)
                .map(_ => Success[GrantTopicAccess])
          }
      }
    )

}
