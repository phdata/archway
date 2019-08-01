package io.phdata.repositories

import java.time.Instant

import cats.data.OptionT
import io.phdata.models.Yarn
import doobie.ConnectionIO

trait YarnRepository {
  def create(yarn: Yarn): ConnectionIO[Long]

  def complete(id: Long, time: Instant): ConnectionIO[Int]

  def find(id: Long): OptionT[ConnectionIO, Yarn]

  def findByWorkspaceId(id: Long): ConnectionIO[List[Yarn]]
}
