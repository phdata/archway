package com.heimdali.repositories

import com.heimdali.test.fixtures._
import doobie.implicits._
import org.scalatest.{FlatSpec, Matchers}

class HiveDatabaseRepositorySpec extends FlatSpec with Matchers with DBTest {

  behavior of "Hive Database Repository"

  it should "Save and extract a record just fine" in {
    val updatedLDAP = new LDAPRepositoryImpl().create(savedLDAP).transact(transactor).unsafeRunSync()

    val repository = new HiveDatabaseRepositoryImpl
    repository.create(savedHive.copy(managingGroup = updatedLDAP)).transact(transactor).unsafeRunSync()

    sql"delete from ldap_registration".update.run.transact(transactor)
    sql"delete from hive_database".update.run.transact(transactor)
  }
}