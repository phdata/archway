package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.Approval
import doobie.free.connection.ConnectionIO

trait ApprovalRepository {
  def insert(id: Long, approval: Approval): ConnectionIO[Approval]

  def find(id: Long): OptionT[ConnectionIO, Approval]
}

class ApprovalRepositoryImpl
  extends ApprovalRepository {
  override def insert(id: Long, approval: Approval): ConnectionIO[Approval] = ???

  override def find(id: Long): OptionT[ConnectionIO, Approval] = ???
}
