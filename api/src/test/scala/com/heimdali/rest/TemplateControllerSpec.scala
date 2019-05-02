package com.heimdali.rest

import cats.effect.IO
import cats.implicits._
import com.heimdali.generators._
import com.heimdali.services.{ConfigService, TemplateService}
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

    templateService.workspaceFor _ expects(*, "simple") returning initialWorkspaceRequest.pure[IO]

    val response = templateController.route.orNotFound.run(POST(request, Uri.uri("/simple")).unsafeRunSync()).unsafeRunSync()
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService
    val configService: ConfigService[IO] = new TestConfigService
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)
    val applicationGenerator: ApplicationGenerator[IO] = new DefaultApplicationGenerator[IO](appConfig, ldapGroupGenerator)
    val topicGenerator: TopicGenerator[IO] = new DefaultTopicGenerator[IO](appConfig, ldapGroupGenerator)
    val templateService: TemplateService[IO] = mock[TemplateService[IO]]

    lazy val templateController: TemplateController[IO] = new TemplateController[IO](authService, templateService)
  }

}
