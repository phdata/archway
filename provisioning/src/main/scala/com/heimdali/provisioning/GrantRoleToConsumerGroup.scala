package com.heimdali.provisioning

import java.time.Instant

import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.Kafka
import doobie.implicits._
import org.apache.sentry.core.model.kafka.ConsumerGroup

case class GrantRoleToConsumerGroup(applicationId: Long, consumerGroup: String, roleName: String) {
  val consumerGroupInstance: ConsumerGroup = new ConsumerGroup(consumerGroup)
}

object GrantRoleToConsumerGroup {

  implicit val viewer: Show[GrantRoleToConsumerGroup] =
    Show.show(g => s"granting role ${g.roleName} rights to consumer group ${g.consumerGroup}")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, GrantRoleToConsumerGroup] =
    ProvisionTask.instance { grant =>
      Kleisli { case (id, context) =>
        Effect[F].delay {
          context
            .sentryClient
            .grantPrivilege(
              grant.roleName,
              Kafka,
              s"ConsumerGroup=${grant.consumerGroup}->action=ALL"
            )
        }.attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, grant, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .applicationRepository
                  .consumerGroupAccess(grant.applicationId, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, grant)
          }
      }
    }

}
