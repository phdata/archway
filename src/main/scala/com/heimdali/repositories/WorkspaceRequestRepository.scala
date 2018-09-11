package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.WorkspaceRequest
import doobie._

trait WorkspaceRequestRepository {
  def findByUsername(username: String): OptionT[ConnectionIO, WorkspaceRequest]

  def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long]

  def list(username: String): ConnectionIO[List[WorkspaceRequest]]

  def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest]

  def linkHive(workspaceId: Long, hiveDatabaseId: Long): ConnectionIO[Int]

  def linkPool(workspaceId: Long, resourcePoolId: Long): ConnectionIO[Int]

  def linkTopic(workspaceId: Long, KafkaTopicId: Long): ConnectionIO[Int]

  def linkApplication(workspaceId: Long, applicationId: Long): ConnectionIO[Int]

}
