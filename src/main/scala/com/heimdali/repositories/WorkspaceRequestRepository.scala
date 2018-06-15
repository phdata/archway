package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.{Approval, WorkspaceRequest}
import doobie.free.connection.ConnectionIO

trait WorkspaceRequestRepository {

  def create(updatedWorkspace: WorkspaceRequest): ConnectionIO[WorkspaceRequest]

  def list(memberships: List[String]): ConnectionIO[List[WorkspaceRequest]]

  def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest]

  def linkHive(id: Long, hiveId: Long): ConnectionIO[Int]

  def linkYarn(id: Long, yarnId: Long): ConnectionIO[Int]

}
