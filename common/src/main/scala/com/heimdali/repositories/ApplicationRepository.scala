package com.heimdali.repositories

import java.time.Instant

import com.heimdali.models.Application
import doobie._

trait ApplicationRepository {

  def consumerGroupAccess(applicationId: Long, time: Instant): ConnectionIO[Int]

  def create(application: Application): ConnectionIO[Long]

  def findByWorkspaceId(workspaceId: Long): ConnectionIO[List[Application]]

}

