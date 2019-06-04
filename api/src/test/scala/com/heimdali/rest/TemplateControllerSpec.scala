package com.heimdali.rest

import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.generators._
import com.heimdali.models.TemplateRequest
import com.heimdali.services.{ConfigService, JSONTemplateService, TemplateService}
import com.heimdali.test.TestAuthService
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.java8.time._
import io.circe.syntax._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.concurrent.ExecutionContext

class TemplateControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest
    with Http4sDsl[IO]
    with AppContextProvider {

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "Template controller"

  it should "generate a simple workspace" in new Http4sClientDsl[IO] {
    implicit val timer: Timer[IO] = testTimer

    val table = Table(
      ("name", "request"),
      ("user", TemplateRequest(standardUsername, standardUsername, standardUsername, initialCompliance, standardUserDN)),
      ("simple", TemplateRequest("Sesame Test #1", "A test", "With details", initialCompliance, standardUserDN)),
      ("structured", TemplateRequest("Sesame Test #1", "A test", "With details", initialCompliance, standardUserDN)),
    )

    forAll(table) { (name, request) =>
      val context: AppContext[IO] = genMockContext()
      val authService: TestAuthService = new TestAuthService
      val configService: ConfigService[IO] = new TestConfigService
      val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)
      val applicationGenerator: ApplicationGenerator[IO] = new DefaultApplicationGenerator[IO](appConfig, ldapGroupGenerator)
      val topicGenerator: TopicGenerator[IO] = new DefaultTopicGenerator[IO](appConfig, ldapGroupGenerator)
      val templateService: TemplateService[IO] = new JSONTemplateService[IO](context, configService)

      val templateController: TemplateController[IO] = new TemplateController[IO](authService, templateService)

      val response = templateController.route.orNotFound.run(POST(request.asJson, Uri.unsafeFromString(s"/$name")).unsafeRunSync())

      val expected: Json = fromResource(s"ssp/default/$name.expected.json")
      check(response, Status.Ok, Some(expected.asObject.get.add("requested_date", testTimer.instant.asJson).asJson))
    }
  }

}
