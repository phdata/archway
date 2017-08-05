package com.heimdali.services

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.libs.json.JsString
import play.api.{ConfigLoader, Configuration}

import scala.concurrent.Future

class LDAPAccountServiceSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "LDAPAccountService"

  it should "return a user if one is found and password matches" in {
    val (name, username, password) = ("name", "username", "password")
    val ldapClient = mock[LDAPClient]

    val ldapUser = LDAPUser(name, username, password, Seq.empty)

    (ldapClient.findUser _)
      .expects(username)
      .returning(Future {
        Some(ldapUser)
      })
    val ldapAccountService = new LDAPAccountService(ldapClient, null)
    ldapAccountService.login(username, password) map { maybeUser =>
      maybeUser shouldBe defined
      maybeUser.get.username should be(username)
    }
  }

  it should "return nothing if a user is found but the password doesn't match" in {
    val (name, username, actualPassword, wrongPassword) = ("name", "username", "password", "passw0rd")
    val ldapClient = mock[LDAPClient]

    val ldapUser = LDAPUser(name, username, actualPassword, Seq.empty)

    (ldapClient.findUser _)
      .expects(username)
      .returning(Future {
        Some(ldapUser)
      })
    val ldapAccountService = new LDAPAccountService(ldapClient, null)
    ldapAccountService.login(username, wrongPassword) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "return nothing if a user is not found" in {
    val (wrongUsername, password) = ("user", "password")
    val ldapClient = mock[LDAPClient]

    (ldapClient.findUser _)
      .expects(wrongUsername)
      .returning(Future {
        None
      })
    val ldapAccountService = new LDAPAccountService(ldapClient, null)
    ldapAccountService.login(wrongUsername, password) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "generate a token" in {
    val secret = "abc"
    val user = User("Dude Doe", "username", HeimdaliRole.BasicUser)

    val configuration = mock[Configuration]
    (configuration.get[String](_: String)(_: ConfigLoader[String]))
      .expects("play.crypto.secret", *).returning(secret)

    val lDAPAccountService = new LDAPAccountService(null, configuration)
    lDAPAccountService.refresh(user).map { token =>
      val accessToken = JwtJson.decodeJson(token.accessToken, secret, Seq(JwtAlgorithm.HS256))
      accessToken.toOption shouldBe defined
      accessToken.get.value should be (Map(
        "name" -> JsString(user.name),
        "username" -> JsString(user.username),
        "role" -> JsString(user.role.name)
      ))

      val refreshToken = JwtJson.decodeJson(token.refreshToken, secret, Seq(JwtAlgorithm.HS256))
      refreshToken.toOption shouldBe defined
      refreshToken.get.value should be (Map("username" -> JsString(user.username)))
    }
  }

}
