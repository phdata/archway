package com.heimdali.repositories

import com.heimdali.models.GovernedDataset

import scala.concurrent.Future

trait GovernedDatasetRepository {
  def find(names: Seq[String]): Future[Seq[GovernedDataset]]

  def create(governedDataset: GovernedDataset): Future[GovernedDataset]

  def get(id: Long): Future[Option[GovernedDataset]]
}

