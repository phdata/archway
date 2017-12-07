package com.heimdali.services

import com.typesafe.config.Config
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.Future

class LDAPAccountServiceSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "LDAPAccountService"

  it should "return a user if one is found and password matches" in {
    val (name, username, password) = ("name", "username", "password")
    val ldapClient = mock[LDAPClient]

    val ldapUser = LDAPUser(name, username, password, Seq.empty)

    (ldapClient.findUser _)
      .expects(username, password)
      .returning(Future {
        Some(ldapUser)
      })
    val ldapAccountService = new LDAPAccountService(ldapClient, null)
    ldapAccountService.login(username, password) map { maybeUser =>
      maybeUser shouldBe defined
    }
  }

  it should "return nothing if a user is found but the password doesn't match" in {
    val (name, username, actualPassword, wrongPassword) = ("name", "username", "password", "passw0rd")
    val ldapClient = mock[LDAPClient]

    val ldapUser = LDAPUser(name, username, actualPassword, Seq.empty)

    (ldapClient.findUser _)
      .expects(username, wrongPassword)
      .returning(Future {
        None
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
      .expects(wrongUsername, password)
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
    val user = User("Dude Doe", "username")

    val configuration = mock[Config]
    (configuration.getString _)
      .expects("play.crypto.secret").returning(secret)

    val lDAPAccountService = new LDAPAccountService(null, configuration)
    lDAPAccountService.refresh(user).map { token =>
      val accessToken = JwtCirce.decodeJson(token.accessToken, secret, Seq(JwtAlgorithm.HS256))
      accessToken.toOption shouldBe defined
      accessToken.get.as[Map[String, String]] should be (Map(
        "name" -> user.name,
        "username" -> user.username
      ))

      val refreshToken = JwtCirce.decodeJson(token.refreshToken, secret, Seq(JwtAlgorithm.HS256))
      refreshToken.toOption shouldBe defined
      refreshToken.get.as[Map[String, String]] should be (Map("username" -> user.username))
    }
  }

  it should "validate a valid token" in {
    val secret = "1yR2kSbv$gE@xkQRdlTtuecW"
    val user = User("Dude Doe", "username")
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicm9sZSI6InVzZXIifQ.ArnfmWsJCLLoqGR2jKWQGJAf5kxP7Um2njCykIM5XXQ"

    val configuration = mock[Config]
    (configuration.getString _)
      .expects("play.crypto.secret").returning(secret)

    val lDAPAccountService = new LDAPAccountService(null, configuration)
    lDAPAccountService.validate(token).map { maybeUser =>
      maybeUser shouldBe defined
      maybeUser.get should be (user)
    }
  }

}
