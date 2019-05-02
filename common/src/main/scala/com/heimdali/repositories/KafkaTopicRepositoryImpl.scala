package com.heimdali.repositories

import java.time.{Clock, Instant}

import com.heimdali.models.{KafkaTopic, LDAPRegistration, TopicGrant}
import doobie._
import doobie.implicits._

class KafkaTopicRepositoryImpl extends KafkaTopicRepository {

  override def create(kafkaTopic: KafkaTopic): ConnectionIO[Long] =
    Statements.create(kafkaTopic).withUniqueGeneratedKeys("id")

  override def topicCreated(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .topicCreated(id, time)
      .run

  private def grant(role: Statements.KafkaGrantHeader, ldap: LDAPRegistration, attributes: List[LDAPAttribute]) =
    TopicGrant(
      role.name,
      ldap.copy(attributes = attributes.map(a => a.key -> a.value).distinct),
      role.actions,
      role.id,
      role.topicAccess
    )

  override def findByWorkspaceId(workspaceId: Long): doobie.ConnectionIO[List[KafkaTopic]] =
    Statements
      .findByWorkspaceId(workspaceId).
      to[List]
    .map(_.groupBy(r => (r._1, r._2, r._3, r._5, r._6)).map {
      case ((header, manager, managerLDAP, readonly, readonlyLDAP), group) =>
        KafkaTopic(
          header.name,
          header.partitions,
          header.replicationFactor,
          grant(manager, managerLDAP, group.map(_._4)),
          grant(readonly, readonlyLDAP, group.map(_._7)),
          header.id
        )
    }.toList)

  object Statements {

    case class KafkaHeader(name: String, partitions: Int, replicationFactor: Int, id: Option[Long], topicCreated: Option[Instant])

    case class KafkaGrantHeader(name: String, actions: String, id: Option[Long], topicAccess: Option[Instant])

    type KafkaRecord = (KafkaHeader, KafkaGrantHeader, LDAPRecord, LDAPAttribute, KafkaGrantHeader, LDAPRecord, LDAPAttribute)

    def create(kafkaTopic: KafkaTopic): Update0 =
      sql"""
         insert into kafka_topic (name, partitions, replication_factor, manager_role_id, readonly_role_id)
         values (${kafkaTopic.name}, ${kafkaTopic.partitions}, ${kafkaTopic.replicationFactor}, ${kafkaTopic.managingRole.id}, ${kafkaTopic.readonlyRole.id})
        """.update

    def topicCreated(id: Long, time: Instant): Update0 =
      sql"""
         update kafka_topic
         set topic_created = $time
         where id = $id
        """.update

    def findByWorkspaceId(workspaceId: Long): Query0[KafkaRecord] =
      sql"""
         select
            k.name,
            k.partitions,
            k.replication_factor,
            k.id,
            k.topic_created,

            k.name,
            mg.actions,
            mg.id,
            mg.topic_access,

            ml.distinguished_name,
            ml.common_name,
            ml.sentry_role,
            ml.id,
            ml.group_created,
            ml.role_created,
            ml.group_associated,

            ma.attr_key,
            ma.attr_value,

            k.name,
            rg.actions,
            rg.id,
            rg.topic_access,

            rl.distinguished_name,
            rl.common_name,
            rl.sentry_role,
            rl.id,
            rl.group_created,
            rl.role_created,
            rl.group_associated,

            ra.attr_key,
            ra.attr_value
         from kafka_topic k
         inner join workspace_topic wt on wt.kafka_topic_id = k.id

         inner join topic_grant mg on k.manager_role_id = mg.id
         inner join ldap_registration ml on mg.ldap_registration_id = ml.id
         inner join ldap_attribute ma on ma.ldap_registration_id = ml.id

         inner join topic_grant rg on k.readonly_role_id = rg.id
         inner join ldap_registration rl on rg.ldap_registration_id = rl.id
         inner join ldap_attribute ra on ra.ldap_registration_id = rl.id

         where
            wt.workspace_request_id = $workspaceId
        """.query[KafkaRecord]

  }

}
