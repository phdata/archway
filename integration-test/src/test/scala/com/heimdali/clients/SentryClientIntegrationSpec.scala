package com.heimdali.clients

import cats.effect.{ContextShift, IO}
import com.heimdali.itest.fixtures.{HiveTest, IntegrationTest}
import com.heimdali.services.UGILoginContextProvider
import com.heimdali.itest.fixtures._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class SentryClientIntegrationSpec extends FlatSpec with Matchers with HiveTest with IntegrationTest with KerberosTest {

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "Sentry Client"

  it should "list roles" in {
    val client = new SentryClientImpl[IO](hiveTransactor, null, new UGILoginContextProvider(itestConfig))
    val result = client.roles.unsafeRunSync()
  }

}

