package io.phdata.repositories

import doobie.free.connection.ConnectionIO
import io.phdata.models.ComplianceGroup

trait ComplianceGroupRepository {

  def create(complianceGroup: ComplianceGroup): ConnectionIO[Long]

  def update(complianceGroupId: Long, complianceGroup: ComplianceGroup): ConnectionIO[Int]

  def list: ConnectionIO[List[ComplianceGroup]]

  def delete(complianceGroupId: Long): ConnectionIO[Unit]
}
