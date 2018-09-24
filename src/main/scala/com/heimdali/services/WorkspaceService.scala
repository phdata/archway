package com.heimdali.services

import cats.data._
import com.heimdali.models._

trait WorkspaceService[F[_]] {
  def find(id: Long): OptionT[F, WorkspaceRequest]

  def list(username: String): F[List[WorkspaceRequest]]

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest]

  def approve(id: Long, approval: Approval): F[Approval]

  def provision(workspace: WorkspaceRequest): F[NonEmptyList[String]]

  def findByUsername(username: String): OptionT[F, WorkspaceRequest]

  def yarnInfo(id: Long): F[List[YarnInfo]]

  def hiveDetails(id: Long): F[List[HiveDatabase]]

}