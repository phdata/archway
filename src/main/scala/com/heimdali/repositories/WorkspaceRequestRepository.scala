package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.{ApproverRole, WorkspaceRequest, WorkspaceSearchResult}
import doobie._

trait WorkspaceRequestRepository {
  def findByUsername(distinguishedName: String): OptionT[ConnectionIO, WorkspaceRequest]

  def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long]

  def list(username: String): ConnectionIO[List[WorkspaceSearchResult]]

  def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest]

  def linkHive(workspaceId: Long, hiveDatabaseId: Long): ConnectionIO[Int]

  def linkPool(workspaceId: Long, resourcePoolId: Long): ConnectionIO[Int]

  def linkTopic(workspaceId: Long, KafkaTopicId: Long): ConnectionIO[Int]

  def linkApplication(workspaceId: Long, applicationId: Long): ConnectionIO[Int]

  def pendingQueue(role: ApproverRole): ConnectionIO[List[WorkspaceSearchResult]]
}
