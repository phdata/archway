package io.phdata.repositories

import cats.data.OptionT
import io.phdata.models.Approval
import doobie._

trait ApprovalRepository {
  def create(id: Long, approval: Approval): ConnectionIO[Approval]

  def find(id: Long): OptionT[ConnectionIO, Approval]

  def findByWorkspaceId(id: Long): ConnectionIO[List[Approval]]
}
