package io.phdata.repositories

import doobie.ConnectionIO
import io.phdata.models.ComplianceQuestion

trait ComplianceRepository {
  def create(compliance: List[ComplianceQuestion], workspaceId: Long): List[ConnectionIO[Long]]
}
