package io.phdata.rest

import java.net.URLEncoder

import cats.effect.{IO, Timer}
import io.phdata.test.fixtures._
import io.circe.Json
import io.circe.java8.time._
import io.circe.syntax._
import io.phdata.AppContext
import io.phdata.generators.{DefaultLDAPGroupGenerator}
import io.phdata.models.TemplateRequest
import io.phdata.services.{ConfigService, JSONTemplateService, TemplateService}
import io.phdata.test.TestAuthService
import io.phdata.test.fixtures.{AppContextProvider, HttpTest, TestConfigService}
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{FlatSpec, Matchers}

class TemplateControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest
    with Http4sDsl[IO]
    with AppContextProvider {

  behavior of "Template controller"

  it should "generate a simple workspace" in new Context {
    val table = Table(
      ("name", "request"),
      ("user", TemplateRequest(standardUsername, standardUsername, standardUsername, initialCompliance, standardUserDN)),
      ("simple", TemplateRequest("Sesame Test #1", "A test", "With details", initialCompliance, standardUserDN)),
      ("structured", TemplateRequest("Sesame Test #1", "A test", "With details", initialCompliance, standardUserDN)),
    )

    forAll(table) { (name, request) =>
      val response = templateController.route.orNotFound.run(POST(request.asJson, Uri.unsafeFromString(s"/$name")).unsafeRunSync())

      val expected: Json = fromResource(s"ssp/default/$name.expected.json")
      check(response, Status.Ok, Some(expected.asObject.get.add("requested_date", testTimer.instant.asJson).asJson))
    }
  }

  it should "return all custom templates" in new Context {
    val response = templateController.route.orNotFound.run(GET(Uri.uri("custom")).unsafeRunSync())

    val expected: Json = fromResource(s"ssp/default/custom/custom-metadata.expected.json")
    check(response, Status.Ok, Some(expected))
  }

  it should "return none custom templates if path is incorrect" in new Context {
    val overriddenTemplateConf = appConfig.templates.copy(templateRoot = "")
    val overriddenAppConfig = appConfig.copy(templates = overriddenTemplateConf)
    val overriddenContext = context.copy(appConfig = overriddenAppConfig)

    override val templateService: TemplateService[IO] = new JSONTemplateService[IO](overriddenContext, configService)
    override val templateController: TemplateController[IO] = new TemplateController[IO](authService, templateService)

    val response = templateController.route.orNotFound.run(GET(Uri.uri("custom")).unsafeRunSync())

    check(response, Status.Ok, Some(Json.arr()))
  }

  it should "generate a custom workspace" in new Context {
    val name = "Custom template 1"
    val request = TemplateRequest(name, "Custom template test", "A custom template test", initialCompliance, standardUserDN)

    val response = templateController.route.orNotFound.run(POST(request.asJson, Uri.unsafeFromString(s"/${URLEncoder.encode(name, "UTF-8")}")).unsafeRunSync())

    val expected: Json = fromResource(s"ssp/default/custom/custom-template-1.expected.json")
    check(response, Status.Ok, Some(expected.asObject.get.add("requested_date", testTimer.instant.asJson).asJson))
  }

  it should "return defaults template" in new Context {
    val response = templateController.route.orNotFound.run(GET(Uri.uri("simple")).unsafeRunSync())

    val expected: Json = fromResource(s"ssp/default/user-defaults.expected.json")
    check(response, Status.Ok, Some(expected))
  }

  trait Context extends Http4sClientDsl[IO]{
    implicit val timer: Timer[IO] = testTimer

    val context: AppContext[IO] = genMockContext()
    val authService: TestAuthService = new TestAuthService
    val configService: ConfigService[IO] = new TestConfigService
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)
    val templateService: TemplateService[IO] = new JSONTemplateService[IO](context, configService)

    val templateController: TemplateController[IO] = new TemplateController[IO](authService, templateService)

  }

}
