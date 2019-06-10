package com.heimdali.repositories

import com.heimdali.common.IntegrationTest
import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class ConfigRepositoryImplIntegrationSpec
  extends FunSuite
    with Matchers
    with DBTest
    with IOChecker
    with IntegrationTest {

  val repo = new ConfigRepositoryImpl()

  test("insert") { check(repo.Statements.update("nextgid", "1039494")) }
  test("select") { check(repo.Statements.select("nexgid")) }

}
