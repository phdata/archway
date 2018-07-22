package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.clients.Kafka
import org.apache.sentry.core.model.kafka.ConsumerGroup
import org.apache.sentry.provider.db.generic.service.thrift.{TAuthorizable, TSentryPrivilege}

import scala.collection.JavaConverters._

case class GrantRoleToConsumerGroup(consumerGroup: String, roleName: String) {
  val consumerGroupInstance: ConsumerGroup = new ConsumerGroup(consumerGroup)
}

object GrantRoleToConsumerGroup {

  implicit val viewer: Show[GrantRoleToConsumerGroup] =
    Show.show(g => s"granting role ${g.roleName} rights to consumer group ${g.consumerGroup}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantRoleToConsumerGroup] =
    ProvisionTask.instance { grant =>
      Kleisli { config =>
        val auth = new TAuthorizable(grant.consumerGroupInstance.getTypeName, grant.consumerGroupInstance.getName)
        F.map(F.attempt(F.delay {
          config
            .hiveClient
            .grantPrivilege(
              grant.roleName,
              Kafka,
              s"ConsumerGroup=${grant.consumerGroup}->action=ALL"
            )
        })) {
          case Left(exception) => Error(grant, exception)
          case Right(_) => Success(grant)
        }
      }
    }

}
