package com.heimdali.repositories

import java.time.Clock

import com.heimdali.models.KafkaTopic
import doobie._
import doobie.implicits._

class KafkaTopicRepositoryImpl(val clock: Clock)
  extends KafkaTopicRepository {

  override def create(kafkaTopic: KafkaTopic): ConnectionIO[Long] =
    Statements.create(kafkaTopic).withUniqueGeneratedKeys("id")

  override def topicCreated(id: Long): ConnectionIO[Int] =
    Statements.topicCreated(id).run

  override def findByWorkspaceId(workspaceId: Long): doobie.ConnectionIO[List[KafkaTopic]] =
    Statements.findByWorkspaceId(workspaceId).to[List]

  object Statements {

    def create(kafkaTopic: KafkaTopic): Update0 =
      sql"""
         insert into kafka_topic (name, partitions, replication_factor, manager_role_id, readonly_role_id)
         values (${kafkaTopic.name}, ${kafkaTopic.partitions}, ${kafkaTopic.replicationFactor}, ${kafkaTopic.managingRole.id}, ${kafkaTopic.readonlyRole.id})
        """.update

    def topicCreated(id: Long): Update0 =
      sql"""
         update kafka_topic
         set topic_created = ${clock.instant}
         where id = $id
        """.update

    def findByWorkspaceId(workspaceId: Long): Query0[KafkaTopic] =
      sql"""
         select
            k.name,
            k.partitions,
            k.replication_factor,

            k.name,
            ml.distinguished_name,
            ml.common_name,
            ml.sentry_role,
            ml.id,
            ml.group_created,
            ml.role_created,
            ml.group_associated,
            mg.actions,
            mg.id,
            mg.topic_access,

            k.name,
            rl.distinguished_name,
            rl.common_name,
            rl.sentry_role,
            rl.id,
            rl.group_created,
            rl.role_created,
            rl.group_associated,
            rg.actions,
            rg.id,
            rg.topic_access,

            k.id,
            null
         from kafka_topic k
         inner join workspace_topic wt on wt.kafka_topic_id = k.id

         inner join topic_grant mg on k.manager_role_id = mg.id
         inner join ldap_registration ml on mg.ldap_registration_id = ml.id

         inner join topic_grant rg on k.readonly_role_id = rg.id
         inner join ldap_registration rl on rg.ldap_registration_id = rl.id

         where
            wt.workspace_request_id = $workspaceId
        """.query[KafkaTopic]

  }

}
