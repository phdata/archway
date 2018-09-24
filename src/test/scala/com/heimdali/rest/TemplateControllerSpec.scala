package com.heimdali.rest

import cats.effect.IO
import com.heimdali.test.fixtures.{HttpTest, TestAuthService, fromResource}
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
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

    val response = templateController.route.orNotFound.run(POST(uri("/simple"), request).unsafeRunSync())
    check(response, Status.Ok, Some(fromResource("rest/templates.simple.post.expected.json")))(jsonDecoder)
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService

    lazy val templateController: TemplateController = new TemplateController(authService)
  }
}
