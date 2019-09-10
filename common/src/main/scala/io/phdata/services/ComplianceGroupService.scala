package io.phdata.services

import io.phdata.models.ComplianceGroup

trait ComplianceGroupService[F[_]] {

  def createComplianceGroup(complianceGroup: ComplianceGroup): F[Long]

  def updateComplianceGroup(complianceGroupId: Long, complianceGroup: ComplianceGroup): F[Unit]

  def list: F[List[ComplianceGroup]]

  def deleteComplianceGroup(complianceGroupId: Long): F[Unit]

  def loadDefaultComplianceQuestions(): F[Unit]
}
