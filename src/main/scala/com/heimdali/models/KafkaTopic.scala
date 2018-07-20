package com.heimdali.models

import java.time.Instant

import cats.effect.Effect
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks.{CreateKafkaTopic, ProvisionTask}
import io.circe._
import io.circe.syntax._

case class TopicGrant(name: String,
                      ldapRegistration: LDAPRegistration,
                      id: Option[Long] = None,
                      topicAccess: Option[Instant] = None)

object TopicGrant {

  implicit def provisioner[F[_] : Effect]: ProvisionTask[F, TopicGrant] =
    ProvisionTask.instance(grant =>
      for {
        grant <- GrantTopicAccess(grant.name, grant.ldapRegistration.sentryRole)
        ldap <- grant.ldapRegistration.provision
      } yield grant |+| ldap
    )

}

case class KafkaTopic(name: String,
                      partitions: Int,
                      replicationFactor: Int,
                      managingRole: TopicGrant,
                      readonlyRole: TopicGrant,
                      id: Option[Long] = None)

object KafkaTopic {

  implicit def provisioner[F[_] : Effect]: ProvisionTask[F, KafkaTopic] =
    ProvisionTask.instance(topic =>
      for {
        create <- CreateKafkaTopic(topic.name, topic.partitions, topic.replicationFactor).provision
        managingRole <- topic.managingRole.provision
        readonlyRole <- topic.readonlyRole.provision
      } yield create |+| managingRole |+| readonlyRole
    )

  implicit val encoder: Encoder[KafkaTopic] =
    Encoder.instance { t =>
      Json.obj(
        "id" -> t.id.asJson,
        "name" -> t.name.asJson,
        "partitions" -> t.partitions.asJson,
        "replication_factor" -> t.replicationFactor.asJson,
        "managing_group" -> t.managingRole.asJson
      )
    }

  implicit val decoder: Decoder[KafkaTopic] =
    Decoder.forProduct5("id", "name", "partitions", "replication_factor", "managing_role")((id: Option[Long], name: String, partitions: Int, replicationFactor: Int, group: LDAPRegistration) => KafkaTopic(name, partitions, replicationFactor, group, id))


}
