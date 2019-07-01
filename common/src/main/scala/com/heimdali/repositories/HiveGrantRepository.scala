package com.heimdali.repositories

import java.time.Instant

import doobie._

trait HiveGrantRepository {

  def create(ldapRegistrationId: Long): ConnectionIO[Long]

  def locationGranted(id: Long, time: Instant): ConnectionIO[Int]

  def databaseGranted(id: Long, time: Instant): ConnectionIO[Int]

}
