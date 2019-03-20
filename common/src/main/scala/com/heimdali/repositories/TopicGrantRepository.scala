package com.heimdali.repositories

import java.time.Instant

import com.heimdali.models.TopicGrant
import doobie._

trait TopicGrantRepository {

  def create(topicGrant: TopicGrant): ConnectionIO[Long]

  def topicAccess(id: Long, time: Instant): ConnectionIO[Int]

}
