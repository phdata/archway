package io.phdata.provisioning

import cats.data.NonEmptyList
import io.phdata.models.{KafkaTopic, LDAPRegistration, TopicGrant}

trait KafkaProvisioning {

  val KafkaTopicProvisionable: Provisionable[KafkaTopic] =
    Provisionable.deriveFromSteps { (kafkaTopic, config) =>
      NonEmptyList.one(
        TypeWith[Provisionable, KafkaTopicRegistration](
          KafkaTopicRegistration(
            kafkaTopic.id.get,
            kafkaTopic.name,
            kafkaTopic.partitions,
            kafkaTopic.replicationFactor
          )
        )(KafkaTopicRegistrationProvisioning.provisionable, KafkaTopicRegistrationProvisioning.show)
      ) ++
        List(
          TypeWith[Provisionable, TopicGrant](
            kafkaTopic.managingRole
          )(TopicGrantProvisionable, TopicGrant.viewer),
          TypeWith[Provisionable, TopicGrant](
            kafkaTopic.readonlyRole
          )(TopicGrantProvisionable, TopicGrant.viewer)
        ).filter(_ => config.kafka.secureTopics)
    }

  val TopicGrantProvisionable: Provisionable[TopicGrant] =
    Provisionable.deriveFromSteps { (topicGrant, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, LDAPRegistration](
          topicGrant.ldapRegistration
        )(LDAPRegistrationProvisionable, LDAPRegistration.show),
        TypeWith[Provisionable, KafkaTopicGrant](
          KafkaTopicGrant(
            topicGrant.id.get,
            topicGrant.name,
            topicGrant.ldapRegistration.sentryRole,
            NonEmptyList.fromListUnsafe(topicGrant.actions.split(",").toList)
          )
        )(KafkaTopicGrantProvisioning.provisionable, KafkaTopicGrantProvisioning.show)
      )
    }

}
