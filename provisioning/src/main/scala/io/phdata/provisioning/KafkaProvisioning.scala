package io.phdata.provisioning

import cats.data.NonEmptyList
import io.phdata.models.{KafkaTopic, LDAPRegistration, TopicGrant}

trait KafkaProvisioning {

  implicit val KafkaTopicProvisionable: Provisionable[KafkaTopic] =
    Provisionable.deriveFromSteps { (kafkaTopic, config) =>
      NonEmptyList.one(
        TypeWith[Provisionable, KafkaTopicRegistration](
          KafkaTopicRegistration(
            kafkaTopic.id.get,
            kafkaTopic.name,
            kafkaTopic.partitions,
            kafkaTopic.replicationFactor
          )
        )
      ) ++
        List(
          TypeWith[Provisionable, TopicGrant](
            kafkaTopic.managingRole
          ),
          TypeWith[Provisionable, TopicGrant](
            kafkaTopic.readonlyRole
          )
        ).filter(_ => config.kafka.secureTopics)
    }

  implicit val TopicGrantProvisionable: Provisionable[TopicGrant] =
    Provisionable.deriveFromSteps { (topicGrant, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, LDAPRegistration](
          topicGrant.ldapRegistration
        ),
        TypeWith[Provisionable, KafkaTopicGrant](
          KafkaTopicGrant(
            topicGrant.id.get,
            topicGrant.name,
            topicGrant.ldapRegistration.sentryRole,
            NonEmptyList.fromListUnsafe(topicGrant.actions.split(",").toList)
          )
        )
      )
    }

}
