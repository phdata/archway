package com.heimdali.repositories

import com.heimdali.models.HiveDatabase

import scala.concurrent.Future

trait HiveDatabaseRepository {
  def create(hiveDatabase: HiveDatabase): Future[HiveDatabase]
}

