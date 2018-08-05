package com.heimdali.services

import cats.data._
import com.heimdali.models._
import com.heimdali.repositories._

trait WorkspaceService[F[_]] {

  def find(id: Long): OptionT[F, WorkspaceRequest]

  def list(username: String): F[List[WorkspaceRequest]]

  def create(workspace: WorkspaceRequest): F[WorkspaceRequest]

  def approve(id: Long, approval: Approval): F[Approval]

  def provision(workspace: WorkspaceRequest): F[NonEmptyList[String]]

}
