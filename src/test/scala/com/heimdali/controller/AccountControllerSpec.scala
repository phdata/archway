package com.heimdali.controller

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import com.heimdali.services._
import com.heimdali.startup.Startup
import com.heimdali.test.fixtures.{LDAPTest, TestStartup}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AccountControllerSpec
  extends FlatSpec
    with WsScalaTestClient
    with Matchers
    with LDAPTest
    with GuiceOneAppPerSuite {

  lazy val secret: String = app.configuration.get[String]("play.crypto.secret")

  behavior of "AccountController"

  it should "do something" in {
    val rootCall = login

    val content = contentAsString(rootCall)
    status(rootCall) should be(OK)

    val json = contentAsJson(rootCall)

    val accessToken = (json \ "accessToken").asOpt[String]
    accessToken shouldBe defined
    val accessTokenPayload = JwtJson.decodeJson(accessToken.get, secret, Seq(JwtAlgorithm.HS256))
    accessTokenPayload.get.value should be (Map(
    "name" -> JsString("Dude Doe"),
    "username" -> JsString("username"),
    "role" -> JsString(HeimdaliRole.BasicUser.name)
    ))

    val refreshToken = (json \ "refreshToken").asOpt[String]
    refreshToken shouldBe defined
    val refreshTokenPayload = JwtJson.decodeJson(refreshToken.get, secret, Seq(JwtAlgorithm.HS256))
    refreshTokenPayload.get.value should be (Map(
    "username" -> JsString("username")
    ))
  }

  it should "get a profile" in {
    val request = FakeRequest(GET, "/account/profile")
      .withHeaders(AUTHORIZATION -> s"Bearer $accessToken")
    val rootCall = route(app, request).get

    status(rootCall) should be (OK)

    contentAsJson(rootCall).as[JsObject].value should be (Map(
      "name" -> JsString("Dude Doe"),
      "username" -> JsString("username"),
      "role" -> JsString(HeimdaliRole.BasicUser.name)
    ))
  }

  private def accessToken = {
    val result = contentAsJson(login)

    (result \ "accessToken").as[String]
  }

  private def login = {
    val auth = "username:password".getBytes(Charsets.UTF_8)
    val encoded = BaseEncoding.base64().encode(auth)
    val request = FakeRequest(GET, "/account/token")
      .withHeaders(AUTHORIZATION -> s"Basic $encoded")

    route(app, request).get
  }

  import play.api.inject.bind
  override val fakeApplication: Application =
    new GuiceApplicationBuilder()
      .overrides(bind[Startup].to[TestStartup].eagerly())
      .build()
}