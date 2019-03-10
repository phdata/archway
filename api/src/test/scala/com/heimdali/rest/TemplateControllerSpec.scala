package com.heimdali.rest

import cats.effect.IO
import cats.implicits._
import com.heimdali.generators._
import com.heimdali.models.{SimpleTemplate, StructuredTemplate}
import com.heimdali.services.ConfigService
import com.heimdali.test.TestAuthService
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.java8.time._
import io.circe.syntax._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class TemplateControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "Template controller"

  it should "generate a simple workspace" in new Http4sClientDsl[IO] with Context {
    val request = fromResource("rest/templates.simple.post.json")

    configService.getAndSetNextGid _ expects() returning 123L.pure[IO] repeat 3 times()

    val response = templateController.route.orNotFound.run(POST(request, Uri.uri("/simple")).unsafeRunSync())

    val expected: Json =
      fromResource("rest/templates.simple.post.expected.json")
        .asObject
        .get
        .add("requested_date", clock.instant().asJson)
        .asJson
    check(response, Status.Ok, Some(expected))
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService
    val configService: ConfigService[IO] = mock[ConfigService[IO]]
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](appConfig, configService)
    val applicationGenerator: ApplicationGenerator[IO] = new DefaultApplicationGenerator[IO](appConfig, ldapGroupGenerator)
    val simpleTemplateService: WorkspaceGenerator[IO, SimpleTemplate] = new DefaultSimpleWorkspaceGenerator[IO](appConfig, ldapGroupGenerator, applicationGenerator)
    val structuredTemplateService: WorkspaceGenerator[IO, StructuredTemplate] = new DefaultStructuredWorkspaceGenerator[IO](appConfig, ldapGroupGenerator, applicationGenerator)

    lazy val templateController: TemplateController = new TemplateController(authService, simpleTemplateService, structuredTemplateService)
  }

}
