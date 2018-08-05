package com.heimdali.tasks

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

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantRoleToConsumerGroup] =
    ProvisionTask.instance { grant =>
      Kleisli { config =>
        F.delay {
          config
            .hiveClient
            .grantPrivilege(
              grant.roleName,
              Kafka,
              s"ConsumerGroup=${grant.consumerGroup}->action=ALL"
            )
        }.attempt
          .flatMap {
            case Left(exception) => F.pure(Error(grant, exception))
            case Right(_) =>
              config
                .applicationRepository
                .consumerGroupAccess(grant.applicationId)
                .transact(config.transactor)
                .map(_ => Success(grant))
          }
      }
    }

}
