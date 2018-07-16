package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.HiveDatabase
import doobie.free.connection.ConnectionIO

trait HiveDatabaseRepository {
  def create(hiveDatabase: HiveDatabase): ConnectionIO[HiveDatabase]

  def find(id: Long): OptionT[ConnectionIO, HiveDatabase]

  def findByWorkspace(id: Long): ConnectionIO[List[HiveDatabase]]

  def complete(id: Long): ConnectionIO[Int]

  def directoryCreated(id: Long): ConnectionIO[Int]

  def quotaSet(id: Long): ConnectionIO[Int]

  def databaseCreated(id: Long): ConnectionIO[Int]

  def locationGranted(role: DatabaseRole, id: Long): ConnectionIO[Int]

  def databaseGranted(role: DatabaseRole, id: Long): ConnectionIO[Int]
}

