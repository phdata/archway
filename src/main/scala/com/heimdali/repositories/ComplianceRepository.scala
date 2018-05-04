package com.heimdali.repositories

import com.heimdali.models.Compliance

import scala.concurrent.Future

trait ComplianceRepository {
  def create(compliance: Compliance): Future[Compliance]
}

