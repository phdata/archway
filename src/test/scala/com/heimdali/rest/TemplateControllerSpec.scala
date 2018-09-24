package com.heimdali.rest

import cats.effect.IO
import com.heimdali.test.fixtures._
import io.circe.{Json, JsonObject}
import io.circe._
import io.circe.syntax._
import io.circe.java8.time._
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

    lazy val templateController: TemplateController = new TemplateController(authService)
  }

}
