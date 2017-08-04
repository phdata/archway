package com.heimdali.services

import com.unboundid.ldap.listener.{InMemoryDirectoryServer, InMemoryDirectoryServerConfig, InMemoryListenerConfig}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import play.api.{ConfigLoader, Configuration}

class LDAPClientImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "LDAPClientImpl"

  it should "pull the correct values from configuration" in {
    val baseDN = "dc=jotunn,dc=io"
    val userDN = "ou=marketing"
    val username = "username"
    val password = "password"

    val configuration = mock[Configuration]
    val innerConfiguration = mock[Configuration]

    val listenerConfig = new InMemoryListenerConfig("test", null, 12345, null, null, null)
    val ldapConfig = new InMemoryDirectoryServerConfig(baseDN)
    ldapConfig.setListenerConfigs(listenerConfig)
    ldapConfig.setSchema(null)
    val inMemoryDatabase = new InMemoryDirectoryServer(ldapConfig)
    inMemoryDatabase.startListening()
    val file = getClass.getResource("/basicUser.ldif")
    inMemoryDatabase.importFromLDIF(false, file.getPath)

    (configuration.get[Configuration](_: String)(_: ConfigLoader[Configuration]))
      .expects("ldap", *)
      .returning(innerConfiguration)
    (innerConfiguration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("server", *)
      .returning("localhost")
    (innerConfiguration.get[Int](_: String)(_: ConfigLoader[Int]))
      .expects("port", *)
      .returning(12345)
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
    client.findUser(username) map { maybeUser =>
      maybeUser shouldBe defined

      maybeUser.get should have {
        'username ("username")
        'name ("Dude Doe")
        'password ("password")
      }
    }
  }

}
