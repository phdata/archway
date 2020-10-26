package io.phdata.repositories

import java.time.Instant

import cats.data.OptionT
import io.phdata.models.{ApproverRole, DistinguishedName, WorkspaceRequest, WorkspaceSearchResult}
import doobie._

trait WorkspaceRequestRepository {
  def findByDistinguishedName(distinguishedName: DistinguishedName): OptionT[ConnectionIO, WorkspaceRequest]

  def findByName(name: String): OptionT[ConnectionIO, WorkspaceRequest]

  def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long]

  def list(username: DistinguishedName): ConnectionIO[List[WorkspaceSearchResult]]

  def userAccessible(distinguishedName: DistinguishedName): ConnectionIO[List[Long]]

  def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest]

  def findUnprovisioned(): ConnectionIO[List[WorkspaceRequest]]

  def markProvisioned(workspaceId: Long, time: Instant): ConnectionIO[Int]

  def markUnprovisioned(workspaceId: Long): ConnectionIO[Int]

  def linkHive(workspaceId: Long, hiveDatabaseId: Long): ConnectionIO[Int]

  def pendingQueue(role: ApproverRole): ConnectionIO[List[WorkspaceSearchResult]]

  def deleteWorkspace(workspaceId: Long): ConnectionIO[Int]

  def changeOwner(workspaceId: Long, newOwnerDN: DistinguishedName): ConnectionIO[Int]
}
