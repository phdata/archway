package io.phdata.repositories

import io.phdata.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class KafkaTopicRepositoryImplIntegrationSpec
  extends FunSuite
    with Matchers
    with DBTest
    with IOChecker {

  val repo = new KafkaTopicRepositoryImpl()

  test("insert") { check(repo.Statements.create(initialTopic)) }
  test("list") { check(repo.Statements.findByWorkspaceId(id)) }
}
