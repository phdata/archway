package com.heimdali.services

import cats.effect._
import com.heimdali.models.{TemplateRequest, UserDN}
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class JSONTemplateServiceSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  behavior of "JSON Workspace Generator"

  it should "generate valid json" in {
    implicit val timer: Timer[IO] = new TestTimer
    implicit val clock: Clock[IO] = timer.clock
    val context = genMockContext()
    val configService: ConfigService[IO] = new TestConfigService()
    val generator = new JSONTemplateService[IO](context, configService)
    val template = TemplateRequest(name, purpose, purpose, initialCompliance, standardUserDN)
    val result = generator.workspaceFor(template, "simple").unsafeRunSync()
    println(result)
  }

  it should "properly escape backslashes in a requester DN" in {
    val crazyDN = """CN=WHO\+AMI,DC=COMPANY,DC=COM"""
    implicit val timer: Timer[IO] = new TestTimer
    implicit val clock: Clock[IO] = timer.clock
    val context = genMockContext()
    val configService: ConfigService[IO] = new TestConfigService()
    val generator = new JSONTemplateService[IO](context, configService)
    val template = TemplateRequest(name, purpose, purpose, initialCompliance, UserDN(crazyDN))
    val result = generator.workspaceFor(template, "user").unsafeRunSync()
    result.requestedBy.value shouldBe crazyDN
  }

}
