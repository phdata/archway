package com.heimdali.repositories

import java.time.Instant

import doobie._
import doobie.implicits._

class HiveGrantRepositoryImpl extends HiveGrantRepository {

  override def create(ldapRegistrationId: Long): ConnectionIO[Long] =
    Statements
      .insert(ldapRegistrationId)
      .withUniqueGeneratedKeys("id")

  override def locationGranted(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .updateLocationAccess(id, time)
      .run

  override def databaseGranted(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .updateDatabaseAccess(id, time)
      .run

  object Statements {

    def insert(ldapRegistrationId: Long): Update0 =
      sql"""
         insert into hive_grant (ldap_registration_id)
         values ($ldapRegistrationId)
         """.update

    def updateDatabaseAccess(id: Long, time: Instant): Update0 =
      sql"""
        update hive_grant
        set database_access = $time
        where id = $id
        """.update


    def updateLocationAccess(id: Long, time: Instant): Update0 =
      sql"""
        update hive_grant
        set location_access = $time
        where id = $id
        """.update

  }

}
