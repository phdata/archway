package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.models.TemplateRequest
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class JSONTemplateServiceSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  behavior of "JSON Workspace Generator"

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

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
    val template = TemplateRequest(name, purpose, purpose, initialCompliance, crazyDN)
    val result = generator.workspaceFor(template, "user").unsafeRunSync()
    result.requestedBy shouldBe crazyDN
  }

}
