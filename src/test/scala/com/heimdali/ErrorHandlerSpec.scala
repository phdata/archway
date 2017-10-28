package com.heimdali

import com.heimdali.startup.Startup
import com.heimdali.test.fixtures.TestStartup
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsNumber, JsObject, JsString}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandlerSpec extends FlatSpec with Matchers with GuiceOneAppPerSuite {

  behavior of "Error handler"

  it should "return json" in {
    val message = "some message"
    val code = Some(123)
    val errorHandler = new ErrorHandler
    val response = Await.result(errorHandler.generateError(message, code), Duration.Inf)
    response.as[JsObject].value should be(Map(
      "error" -> JsString(message),
      "error_code" -> JsNumber(code.get)
    ))
  }

  it should "return json when not found" in {
    val request = FakeRequest("GET", "/something")
    val response = route(app, request).get
    val result = contentAsJson(response)
    result.as[JsObject].value should be(Map(
      "error_code" -> JsNumber(404)
    ))
  }

  import play.api.inject.bind
  override val fakeApplication: Application =
    new GuiceApplicationBuilder()
      .overrides(bind[Startup].to[TestStartup].eagerly())
      .build()

}
