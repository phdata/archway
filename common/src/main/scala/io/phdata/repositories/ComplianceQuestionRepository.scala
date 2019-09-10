package io.phdata.repositories

import doobie.free.connection.ConnectionIO
import io.phdata.models.ComplianceQuestion

trait ComplianceQuestionRepository {

  def create(complianceGroupId: Long, complianceQuestion: ComplianceQuestion): ConnectionIO[Long]

  def update(complianceQuestion: ComplianceQuestion): ConnectionIO[Int]

  def delete(complianceQuestionId: Long): ConnectionIO[Unit]

  def findByComplianceGroupId(complianceGroupId: Long): ConnectionIO[List[ComplianceQuestion]]
}
