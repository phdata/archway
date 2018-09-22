package com.heimdali.tasks

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.Kafka
import doobie.implicits._

case class GrantTopicAccess(id: Long, name: String, sentryRole: String, actions: NonEmptyList[String]) {

}

object GrantTopicAccess {

  implicit val show: Show[GrantTopicAccess] =
    Show.show(s => "")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantTopicAccess] =
    ProvisionTask.instance(grant =>
      Kleisli { context =>
        grant.actions.traverse[F, Either[Throwable, Unit]] { action =>
          context
            .sentryClient
            .grantPrivilege(grant.sentryRole, Kafka, s"Topic=${grant.name}->action=$action")
            .attempt
        }
          .map(_.combineAll)
          .flatMap {
            case Left(exception) => F.pure(Error(grant, exception))
            case Right(_) =>
              context
                .topicGrantRepository
                .topicAccess(grant.id)
                .transact(context.transactor)
                .map(_ => Success(grant))
          }
      }
    )

}
