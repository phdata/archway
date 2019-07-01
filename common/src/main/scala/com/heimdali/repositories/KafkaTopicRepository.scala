package com.heimdali.repositories

import java.time.Instant

import com.heimdali.models.KafkaTopic
import doobie._

trait KafkaTopicRepository {

  def create(kafkaTopic: KafkaTopic): ConnectionIO[Long]

  def topicCreated(id: Long, time: Instant): ConnectionIO[Int]

  def findByWorkspaceId(workspaceId: Long): ConnectionIO[List[KafkaTopic]]

}
