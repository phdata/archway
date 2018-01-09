package com.heimdali.services

import com.heimdali.test.fixtures.LDAPTest
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

class LDAPClientImplSpec extends AsyncFlatSpec
  with Matchers
  with AsyncMockFactory
  with LDAPTest {

  behavior of "LDAPClientImpl"

  it should "find a user" in {
    val client = new LDAPClientImpl(config)(executionContext) with OpenLDAPClient
    client.findUser(username, password).map { maybeUser =>
      maybeUser shouldBe defined

      maybeUser.get should have {
        'username ("username")
        'name ("Dude Doe")
      }
    }(executionContext)
  }

  it should "create a group" in {
    val client = new LDAPClientImpl(config)(executionContext) with OpenLDAPClient
    client.createGroup("sesame", "username").map { _ =>
      val entry = client.adminConnectionPool.getConnection.getEntry(s"cn=edh_sw_sesame,$groupDN,$baseDN")
      entry should not be null
    }(executionContext)
  }
}
