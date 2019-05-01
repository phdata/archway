package com.heimdali.generators

import cats.effect._
import cats.effect.implicits._
import com.heimdali.models.{SimpleTemplate, TemplateRequest}
import com.heimdali.services.ConfigService
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._

class JSONWorkspaceGeneratorSpec extends FlatSpec with Matchers {

  behavior of "JSON Workspace Generator"

  it should "" in {
    val configService: ConfigService[IO] = new TestConfigService()
    val generator = new JSONWorkspaceGenerator[IO](appConfig, configService)
    val template = TemplateRequest(name, purpose, purpose, initialCompliance, standardUserDN, "simple")
    val result = generator.workspaceFor(template).unsafeRunSync()
    println(result)
  }

}
