package com.heimdali.repositories

import com.heimdali.models.Compliance
import doobie.free.connection.ConnectionIO

trait ComplianceRepository {
  def create(compliance: Compliance): ConnectionIO[Compliance]
}

