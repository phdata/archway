package com.heimdali.repositories

import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class ApplicationRepositoryImplIntegrationSpec extends FunSuite with Matchers with DBTest with IOChecker {

  val repo = new ApplicationRepositoryImpl()

  test("insert") { check(repo.Statements.insert(initialApplication)) }
  test("list") { check(repo.Statements.findByWorkspaceId(id)) }

}
