package com.heimdali.services

import com.heimdali.LDAPTest
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll, Matchers}
import play.api.{ConfigLoader, Configuration}

class LDAPClientImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory with LDAPTest with BeforeAndAfterAll {

  behavior of "LDAPClientImpl"

  it should "pull the correct values from configuration" in {
    val configuration = mock[Configuration]
    val innerConfiguration = mock[Configuration]

    (configuration.get[Configuration](_: String)(_: ConfigLoader[Configuration]))
      .expects("ldap", *)
      .returning(innerConfiguration)
    (innerConfiguration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("server", *)
      .returning("localhost")
    (innerConfiguration.get[Int](_: String)(_: ConfigLoader[Int]))
      .expects("port", *)
      .returning(ldapPort)
    (innerConfiguration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("bind-dn", *)
      .returning(bindDN)
    (innerConfiguration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("bind-password", *)
      .returning(bindPassword)
    (innerConfiguration.getOptional[Int](_: String)(_: ConfigLoader[Int]))
      .expects("connections", *)
      .returning(None)
    (innerConfiguration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("base-dn", *)
      .returning(baseDN)
    (innerConfiguration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("users-path", *)
      .returning(userDN)

    val client = new LDAPClientImpl(configuration)
    client.findUser(username, password) map { maybeUser =>
      maybeUser shouldBe defined

      maybeUser.get should have {
        'username ("username")
        'name ("Dude Doe")
        'password ("password")
      }
    }
  }

  override val ldapPort = 12345

  override protected def beforeAll(): Unit = {
    inMemoryServer.startListening()
  }
}
