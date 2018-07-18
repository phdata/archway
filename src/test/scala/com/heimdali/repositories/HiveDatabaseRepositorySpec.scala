package com.heimdali.repositories

import java.time.{Clock, Instant, ZoneId}

import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class HiveDatabaseRepositorySpec extends FunSuite with Matchers with DBTest with IOChecker {

  val repo = new HiveDatabaseRepositoryImpl(Clock.fixed(Instant.now, ZoneId.of("UTC")))

  test("insert") { check(repo.Statements.insert(initialHive)) }
  test("find") { check(repo.Statements.find(id)) }
  test("list") { check(repo.Statements.list(id)) }

}