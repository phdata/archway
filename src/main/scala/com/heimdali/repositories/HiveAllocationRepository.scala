package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.HiveAllocation
import doobie.free.connection.ConnectionIO

trait HiveAllocationRepository {
  def create(hiveDatabase: HiveAllocation): ConnectionIO[Long]

  def find(id: Long): OptionT[ConnectionIO, HiveAllocation]

  def findByWorkspace(id: Long): ConnectionIO[List[HiveAllocation]]

  def directoryCreated(id: Long): ConnectionIO[Int]

  def quotaSet(id: Long): ConnectionIO[Int]

  def databaseCreated(id: Long): ConnectionIO[Int]
}

