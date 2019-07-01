package com.heimdali.repositories

import java.time.Instant

import com.heimdali.models.HiveAllocation
import doobie.free.connection.ConnectionIO

trait HiveAllocationRepository {
  def databaseCreated(workspaceId: Long, Instant: Any) = ???
  def find(id: Long): ConnectionIO[Option[HiveAllocation]]

  def create(hiveDatabase: HiveAllocation): ConnectionIO[Long]

  def findByWorkspace(id: Long): ConnectionIO[List[HiveAllocation]]

  def directoryCreated(id: Long, time: Instant): ConnectionIO[Int]

  def quotaSet(id: Long, time: Instant): ConnectionIO[Int]

  def databaseCreated(id: Long, time: Instant): ConnectionIO[Int]
}
