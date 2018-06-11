package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models.Yarn
import doobie.ConnectionIO

trait YarnRepository {
  def create(yarn: Yarn): ConnectionIO[Yarn]

  def complete(id: Long): ConnectionIO[Int]

  def find(id: Long): OptionT[ConnectionIO, Yarn]
}
