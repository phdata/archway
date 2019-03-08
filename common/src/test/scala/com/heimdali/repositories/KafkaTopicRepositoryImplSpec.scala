package com.heimdali.repositories

import java.time.{Clock, Instant, ZoneId}

import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class KafkaTopicRepositoryImplSpec extends FunSuite with Matchers with DBTest with IOChecker {

  val repo = new KafkaTopicRepositoryImpl(Clock.fixed(Instant.now, ZoneId.of("UTC")))

  test("insert") { check(repo.Statements.create(initialTopic)) }
  test("list") { check(repo.Statements.findByWorkspaceId(id)) }
}
