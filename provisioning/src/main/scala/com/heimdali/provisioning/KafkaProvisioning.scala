package com.heimdali.provisioning

import cats.Show
import cats.data.{NonEmptyList, WriterT}
import cats.effect.{Clock, Sync}
import cats.implicits._
import com.heimdali.models.{KafkaTopic, TopicGrant}
import com.heimdali.provisioning.Provisionable.ops._

trait KafkaProvisioning {

  implicit object KafkaTopicProvisionable extends Provisionable[KafkaTopic] {

    override def provision[F[_] : Clock : Sync](kafkaTopic: KafkaTopic, workspaceContext: WorkspaceContext[F])(implicit show: Show[KafkaTopic]): WriterT[F, NonEmptyList[Message], ProvisionResult] = {
      if (workspaceContext.context.appConfig.kafka.secureTopics)
        for {
          newTopic <- CreateKafkaTopic(kafkaTopic.id.get, kafkaTopic.name, kafkaTopic.partitions, kafkaTopic.replicationFactor).provision[F](workspaceContext)
          managingRole <- kafkaTopic.managingRole.provision[F](workspaceContext)
          readonlyRole <- kafkaTopic.readonlyRole.provision[F](workspaceContext)
        } yield newTopic |+| managingRole |+| readonlyRole
      else
        CreateKafkaTopic(kafkaTopic.id.get, kafkaTopic.name, kafkaTopic.partitions, kafkaTopic.replicationFactor).provision[F](workspaceContext)
    }
  }

  implicit object TopicGrantProvisionable extends Provisionable[TopicGrant] {

    override def provision[F[_] : Clock : Sync](topicGrant: TopicGrant, workspaceContext: WorkspaceContext[F])(implicit show: Show[TopicGrant]): WriterT[F, NonEmptyList[Message], ProvisionResult] =
      for {
        ldap <- topicGrant.ldapRegistration.provision(workspaceContext)
        access <- GrantTopicAccess(topicGrant.id.get, topicGrant.name, topicGrant.ldapRegistration.sentryRole, NonEmptyList.fromListUnsafe(topicGrant.actions.split(",").toList)).provision[F](workspaceContext)
      } yield access |+| ldap
  }

}
