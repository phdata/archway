package io.phdata.repositories

import java.time.Instant

import io.phdata.models.Application
import doobie._

trait ApplicationRepository {

  def consumerGroupAccess(applicationId: Long, time: Instant): ConnectionIO[Int]

  def create(application: Application): ConnectionIO[Long]

  def findByWorkspaceId(workspaceId: Long): ConnectionIO[List[Application]]

}
