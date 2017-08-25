package com.heimdali.controller

import javax.inject.Inject

import com.heimdali.services._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}

class ClusterControllerSpec
  extends FlatSpec
    with Matchers
    with GuiceOneAppPerSuite {

  behavior of "Configuration Controller"

  it should "get a list of clusters" in {
    val request = FakeRequest(GET, "/cluster")
    val response = route(app, request).get

    status(response) should be(OK)

    val json = contentAsJson(response)

    json shouldBe a[JsArray]
    val items = json.as[JsArray].value

    items.size should be(1)
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[ClusterService].to[FakeClusterService])
      .overrides(bind[AccountService].to[PassiveAccountService])
      .build()

}
