package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.WorkspaceRequest
import doobie._

trait WorkspaceRequestRepository {

  def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long]

  def list(username: String): ConnectionIO[List[WorkspaceRequest]]

  def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest]

}
