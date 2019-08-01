package io.phdata.repositories

import java.time.Instant

import io.phdata.models.TopicGrant
import doobie._

trait TopicGrantRepository {

  def create(topicGrant: TopicGrant): ConnectionIO[Long]

  def topicAccess(id: Long, time: Instant): ConnectionIO[Int]

}
