package com.heimdali.models

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.tasks.{Error, ProvisionTask, Success}
import org.apache.sentry.provider.db.generic.tools.KafkaTSentryPrivilegeConverter

case class GrantTopicAccess(name: String, sentryRole: String) {
  val permission = new KafkaTSentryPrivilegeConverter("kafka", "kafka")
}

object GrantTopicAccess {

  implicit val show: Show[GrantTopicAccess] =
    Show.show(s => "")

  implicit def provisioner[F[_] : Effect](implicit F: Effect[F]): ProvisionTask[F, GrantTopicAccess] =
    ProvisionTask.instance(grant =>
      Kleisli(context =>
        F.map(F.attempt(F.delay(
          context
            .sentryClient
            .grantPrivilege(
              "heimdali_api",
              grant.sentryRole,
              "kafka",
              grant.permission.fromString(""))))) {
          case Left(exc) => Error(exc)
          case Right(_) =>
            context
              .kafkaRepository.
        }
      )
    )

}
