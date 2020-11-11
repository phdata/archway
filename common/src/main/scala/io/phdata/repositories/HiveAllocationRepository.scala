package io.phdata.repositories

import java.time.Instant

import io.phdata.models.HiveAllocation
import doobie.free.connection.ConnectionIO

trait HiveAllocationRepository {

  def find(id: Long): ConnectionIO[Option[HiveAllocation]]

  def create(hiveDatabase: HiveAllocation): ConnectionIO[Long]

  def findByWorkspace(id: Long): ConnectionIO[List[HiveAllocation]]

  def databaseCreated(id: Long, time: Instant): ConnectionIO[Int]

}
