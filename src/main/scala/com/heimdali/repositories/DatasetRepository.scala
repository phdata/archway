package com.heimdali.repositories

import com.heimdali.models.Dataset
import com.heimdali.provisioning.WorkspaceRepository

import scala.concurrent.Future

trait DatasetRepository extends WorkspaceRepository[Dataset] {
  def create(dataset: Dataset): Future[Dataset]

  def setLDAP(datasetId: String, ldapRegistrationId: Long): Future[Dataset]

  def setHive(datasetId: String, hiveDatbaseId: Long): Future[Dataset]
}

