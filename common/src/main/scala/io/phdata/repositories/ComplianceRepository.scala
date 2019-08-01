package io.phdata.repositories

import io.phdata.models.Compliance
import doobie.free.connection.ConnectionIO

trait ComplianceRepository {
  def create(compliance: Compliance): ConnectionIO[Compliance]
}
