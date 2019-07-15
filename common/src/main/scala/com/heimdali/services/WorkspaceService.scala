package com.heimdali.services

import cats.data._
import com.heimdali.models._

trait WorkspaceService[F[_]] {
  def find(id: Long): OptionT[F, WorkspaceRequest]

  def list(distinguishedName: String): F[List[WorkspaceSearchResult]]

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest]

  def approve(id: Long, approval: Approval): F[Approval]

  def findByUsername(distinguishedName: String): OptionT[F, WorkspaceRequest]

  def yarnInfo(id: Long): F[List[YarnInfo]]

  def hiveDetails(id: Long): F[List[HiveDatabase]]

  def reviewerList(role: ApproverRole): F[List[WorkspaceSearchResult]]

  def deleteWorkspace(workspaceId: Long): F[Unit]
}
