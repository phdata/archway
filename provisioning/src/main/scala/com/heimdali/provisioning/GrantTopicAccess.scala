package com.heimdali.provisioning

import java.time.Instant

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

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, GrantTopicAccess] =
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
            case Left(exception) => Effect[F].pure(Error(grant, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .topicGrantRepository
                  .topicAccess(grant.id, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(grant)
          }
      }
    )

}
