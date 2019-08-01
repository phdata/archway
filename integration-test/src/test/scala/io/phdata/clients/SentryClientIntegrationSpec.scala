package io.phdata.clients

import java.util.UUID

import cats.effect.IO
import io.phdata.itest.fixtures.{HiveTest, IntegrationTest, _}
import io.phdata.services.UGILoginContextProvider
import org.scalatest.{FlatSpec, Matchers}

class SentryClientIntegrationSpec extends FlatSpec with Matchers with HiveTest with IntegrationTest with KerberosTest {

  behavior of "Sentry Client"

  it should "list roles" in {
    val client = new SentryClientImpl[IO](hiveTransactor, null, new UGILoginContextProvider(itestConfig))

    val testRoles = List(
      s"test_role_${UUID.randomUUID().toString.take(8)}",
      s"test_role_${UUID.randomUUID().toString.take(8)}",
      s"test_role_${UUID.randomUUID().toString.take(8)}"
    )

    testRoles.foreach(role =>
      client.createRole(role).unsafeRunSync()
    )

    val result = client.roles.unsafeRunSync()

    result should not be empty
    testRoles.foreach(role =>
      result should contain(role)
    )

    testRoles.foreach(role =>
      client.dropRole(role)
    )
  }

}

