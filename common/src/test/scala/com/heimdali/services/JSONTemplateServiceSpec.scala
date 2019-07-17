package com.heimdali.services

import cats.effect._
import com.heimdali.models.TemplateRequest
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
}
