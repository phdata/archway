package com.heimdali.controller

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import com.heimdali.TestApplicationFactory
import com.heimdali.services.HeimdaliRole
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.play.{BaseOneAppPerSuite, WsScalaTestClient}
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.libs.json.JsString
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AccountControllerSpec
  extends FlatSpec
    with WsScalaTestClient
    with Matchers
    with BaseOneAppPerSuite
    with BeforeAndAfterAll
    with TestApplicationFactory {

  behavior of "AccountController"

  it should "do something" in {
    val secret = app.configuration.get[String]("play.crypto.secret")
    val auth = "username:password".getBytes(Charsets.UTF_8)
    val encoded = BaseEncoding.base64().encode(auth)
    val request = FakeRequest(GET, "/account/token")
      .withHeaders(AUTHORIZATION -> s"Basic $encoded")

    val rootCall = route(app, request).get

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

  override protected def beforeAll(): Unit = {
    inMemoryServer.startListening()
  }
}