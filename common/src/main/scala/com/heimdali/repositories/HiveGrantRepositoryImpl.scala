package com.heimdali.repositories

import java.time.{Clock, Instant}

import com.heimdali.models.HiveGrant
import doobie._
import doobie.implicits._

class HiveGrantRepositoryImpl(val clock: Clock)
  extends HiveGrantRepository {

  override def create(ldapRegistrationId: Long): ConnectionIO[Long] =
    Statements.insert(ldapRegistrationId).withUniqueGeneratedKeys("id")

  override def locationGranted(id: Long): ConnectionIO[Int] =
    Statements.updateLocationAccess(id).run

  override def databaseGranted(id: Long): ConnectionIO[Int] =
    Statements.updateDatabaseAccess(id).run

  object Statements {

    def insert(ldapRegistrationId: Long) : Update0 =
      sql"""
         insert into hive_grant (ldap_registration_id)
         values ($ldapRegistrationId)
         """.update

    def updateDatabaseAccess(id: Long): Update0 =
      sql"""
        update hive_grant
        set database_access = ${Instant.now(clock)}
        where id = $id
        """.update


    def updateLocationAccess(id: Long): Update0 =
      sql"""
        update hive_grant
        set location_access = ${Instant.now(clock)}
        where id = $id
        """.update

  }

}
