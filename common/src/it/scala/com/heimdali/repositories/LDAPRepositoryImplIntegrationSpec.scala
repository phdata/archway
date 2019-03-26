package com.heimdali.repositories

import java.time.Instant

import com.heimdali.repositories.LDAPRepositoryImpl
import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class LDAPRepositoryImplIntegrationSpec extends FunSuite with Matchers with DBTest with IOChecker {

  test("groupAssociated") { check(LDAPRepositoryImpl.Statements.groupAssociated(123, Instant.now)) }
  test("roleCreated") { check(LDAPRepositoryImpl.Statements.roleCreated(123, Instant.now)) }
  test("groupCreated") { check(LDAPRepositoryImpl.Statements.groupCreated(123, Instant.now)) }
  test("findAllApplications") { check(LDAPRepositoryImpl.Statements.findAllApplications(123)) }
  test("findAllTopics") { check(LDAPRepositoryImpl.Statements.findAllTopics(123)) }
  test("findAllData") { check(LDAPRepositoryImpl.Statements.findAllData(123)) }
  test("findApplications") { check(LDAPRepositoryImpl.Statements.findApplications(123, "manager")) }
  test("findTopics") { check(LDAPRepositoryImpl.Statements.findTopics(123, "manager")) }
  test("findData") { check(LDAPRepositoryImpl.Statements.findData(123, "manager")) }
  test("insert") { check(LDAPRepositoryImpl.Statements.insert(savedLDAP)) }

}
