package com.heimdali.services

import com.heimdali.test.fixtures.LDAPTest
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

class LDAPClientImplSpec extends AsyncFlatSpec
  with Matchers
  with AsyncMockFactory
  with LDAPTest {

  behavior of "LDAPClientImpl"

  it should "find a user" in {
    val client = new LDAPClientImpl(config)
    client.findUser(username, password) map { maybeUser =>
      maybeUser shouldBe defined

      maybeUser.get should have {
        'username ("username")
        'name ("Dude Doe")
        'password ("password")
      }
    }
  }

  it should "create a group" in {
    val client = new LDAPClientImpl(config)
    client.createGroup("sesame", "username") map { _ =>
      val entry = client.connectionPool.getConnection.getEntry("CN=edh_sw_sesame,OU=groups,OU=hadoop,DC=jotunn,DC=io")
      entry should not be null
    }
  }
}
