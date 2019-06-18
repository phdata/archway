package com.heimdali.repositories

import com.heimdali.models.HiveAllocation
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class HiveAllocationRepositoryIntegrationSpec
  extends FunSuite
    with Matchers
    with DBTest
    with IOChecker {

  val repo = new HiveAllocationRepositoryImpl()

  test("insert") { check(repo.Statements.insert(initialHive)) }
  test("list") {
    val ldapRepo = new LDAPRepositoryImpl()
    val grantRepo = new HiveGrantRepositoryImpl()

    val outcome: ConnectionIO[Option[HiveAllocation]] = for {
      managerLDAP <- ldapRepo.create(initialLDAP.copy(attributes = defaultLDAPAttributes(initialLDAP.distinguishedName, initialLDAP.commonName)))
      managerGrant <- grantRepo.create(managerLDAP.id.get)
      readwriteLDAP <- ldapRepo.create(initialLDAP.copy(attributes = defaultLDAPAttributes(initialLDAP.distinguishedName, initialLDAP.commonName)))
      readwriteGrant <- grantRepo.create(readwriteLDAP.id.get)
      readonlyLDAP <- ldapRepo.create(initialLDAP.copy(attributes = defaultLDAPAttributes(initialLDAP.distinguishedName, initialLDAP.commonName)))
      readonlyGrant <- grantRepo.create(readonlyLDAP.id.get)
      hive <- repo.create(initialHive.copy(
        managingGroup = initialGrant.copy(id = Some(managerGrant), ldapRegistration = managerLDAP),
        readonlyGroup = Some(initialGrant.copy(id = Some(readonlyGrant), ldapRegistration = readonlyLDAP)),
        readWriteGroup = Some(initialGrant.copy(id = Some(readwriteGrant), ldapRegistration = readwriteLDAP))))
      result <- repo.find(hive)
    } yield result

    println(outcome.transact(transactor).unsafeRunSync())
  }

}
