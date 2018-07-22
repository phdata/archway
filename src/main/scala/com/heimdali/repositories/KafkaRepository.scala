package com.heimdali.repositories

import com.heimdali.models.KafkaTopic
import java.time.Clock

import cats.data.OptionT
import doobie._
import doobie.implicits._

trait KafkaRepository {

  def create(kafkaTopic: KafkaTopic): ConnectionIO[Long]

  def topicCreated(id: Long): ConnectionIO[Int]

  def find(id: Long): OptionT[ConnectionIO, KafkaTopic]

  def list(workspaceId: Long): ConnectionIO[List[KafkaTopic]]

}

class KafkaRepositoryImpl(val clock: Clock)
  extends KafkaRepository {

  override def create(kafkaTopic: KafkaTopic): ConnectionIO[Long] =
    Statements.create(kafkaTopic).withUniqueGeneratedKeys("id")

  override def topicCreated(id: Long): ConnectionIO[Int] =
    Statements.topicCreated(id).run

  override def find(id: Long): OptionT[ConnectionIO, KafkaTopic] = ???

  override def list(workspaceId: Long): ConnectionIO[List[KafkaTopic]] = ???

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

    val selectQuery: Fragment =
      sql"""
         select
            k.name,
            k.partitions,
            k.replication_factor,
        """

    def find(id: Long): Query0[KafkaTopic] =
      selectQuery.query

    def list(workspaceId: Long): ConnectionIO[List[KafkaTopic]] = ???

  }

}
