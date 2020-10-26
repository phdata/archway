package io.phdata.repositories

import java.time.Instant

import io.phdata.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class LDAPRepositoryImplIntegrationSpec
  extends FunSuite
    with Matchers
    with DBTest
    with IOChecker {

  test("groupAssociated") { check(LDAPRepositoryImpl.Statements.groupAssociated(123, Instant.now)) }
  test("roleCreated") { check(LDAPRepositoryImpl.Statements.roleCreated(123, Instant.now)) }
  test("groupCreated") { check(LDAPRepositoryImpl.Statements.groupCreated(123, Instant.now)) }
  test("findAllData") { check(LDAPRepositoryImpl.Statements.findAllData(123)) }
  test("findData") { check(LDAPRepositoryImpl.Statements.findData(123, "manager")) }
  test("insert") { check(LDAPRepositoryImpl.Statements.insert(savedLDAP)) }

}
