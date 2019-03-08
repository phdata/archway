package com.heimdali.repositories

import com.heimdali.models.KafkaTopic
import doobie._

trait KafkaTopicRepository {

  def create(kafkaTopic: KafkaTopic): ConnectionIO[Long]

  def topicCreated(id: Long): ConnectionIO[Int]

  def findByWorkspaceId(workspaceId: Long): ConnectionIO[List[KafkaTopic]]

}