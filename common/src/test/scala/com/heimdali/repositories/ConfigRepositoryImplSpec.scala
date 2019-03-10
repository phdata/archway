package com.heimdali.repositories

import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class ConfigRepositoryImplSpec extends FunSuite with Matchers with DBTest with IOChecker {

  val repo = new ConfigRepositoryImpl()

  test("insert") { check(repo.Statements.update("nextgid", "1039494")) }
  test("select") { check(repo.Statements.select("nexgid")) }

}
