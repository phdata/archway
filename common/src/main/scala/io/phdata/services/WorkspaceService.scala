package io.phdata.services

import cats.data._
import io.phdata.models._

trait WorkspaceService[F[_]] {
  def findById(id: Long): OptionT[F, WorkspaceRequest]

  def findByDistinguishedName(distinguishedName: DistinguishedName): OptionT[F, WorkspaceRequest]

  def findByName(name: String): OptionT[F, WorkspaceRequest]

  def list(distinguishedName: DistinguishedName): F[List[WorkspaceSearchResult]]

  def userAccessible(distinguishedName: DistinguishedName, id: Long): F[Boolean]

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest]

  def approve(id: Long, approval: Approval): F[Approval]

  def status(id: Long): F[WorkspaceStatus]

  def yarnInfo(id: Long): F[List[YarnInfo]]

  def hiveDetails(id: Long): F[List[HiveDatabase]]

  def reviewerList(role: ApproverRole): F[List[WorkspaceSearchResult]]

  def deleteWorkspace(workspaceId: Long): F[Unit]

  def changeOwner(workspaceId: Long, newOwnerDN: DistinguishedName): F[Unit]
}
