package com.heimdali.repositories

import com.heimdali.models.Dataset
import com.heimdali.provisioning.WorkspaceRepository

import scala.concurrent.Future

trait DatasetRepository extends WorkspaceRepository[Long, Dataset] {
  def find(id: Long, dataset: String): Future[Option[Dataset]]

  def create(dataset: Dataset): Future[Dataset]

  def setLDAP(datasetId: Long, ldapRegistrationId: Long): Future[Dataset]

  def setHive(datasetId: Long, hiveDatbaseId: Long): Future[Dataset]
}

