package com.heimdali.clients

import cats.effect.IO
import com.heimdali.services.UGILoginContextProvider
import com.heimdali.test.fixtures.HiveTest
import org.scalatest.{FlatSpec, Matchers}

class SentryClientIntegrationSpec extends FlatSpec with Matchers with HiveTest {

  behavior of "Sentry Client"

  it should "list roles" in {
    val client = new SentryClientImpl[IO](hiveTransactor, null, new UGILoginContextProvider())
    val result = client.roles.unsafeRunSync()
  }

}

