package com.heimdali.controller

import com.heimdali.TestApplicationFactory
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.play.{BaseOneAppPerSuite, WsScalaTestClient}

class AccountControllerSpec extends FlatSpec with WsScalaTestClient with Matchers with BaseOneAppPerSuite with TestApplicationFactory {

  behavior of "AccountController"

  it should "do something" in {
//    val rootCall = route(app, FakeRequest(GET, "/")).get
//    contentAsString(rootCall) should include("pong")
  }

}