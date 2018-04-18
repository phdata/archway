package com.heimdali.services

import com.heimdali.repositories.AccountRepository
import com.typesafe.config.{Config, ConfigFactory}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.Future

class AccountServiceImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "LDAPAccountService"

  it should "return a user if one is found and password matches" in {
    val (name, username, password) = ("name", "username", "password")
    val ldapClient = mock[LDAPClient]
    val accountRepository = mock[AccountRepository]

    val ldapUser = LDAPUser(name, username, Seq.empty)

    (ldapClient.findUser _)
      .expects(username, password)
      .returning(Future {
        Some(ldapUser)
      })
    val ldapAccountService = new AccountServiceImpl(ldapClient, accountRepository, ConfigFactory.load())
    ldapAccountService.login(username, password) map { maybeUser =>
      maybeUser shouldBe defined
    }
  }

  it should "return nothing if a user is found but the password doesn't match" in {
    val (name, username, actualPassword, wrongPassword) = ("name", "username", "password", "passw0rd")
    val ldapClient = mock[LDAPClient]
    val accountRepository = mock[AccountRepository]

    val ldapUser = LDAPUser(name, username, Seq.empty)

    (ldapClient.findUser _)
      .expects(username, wrongPassword)
      .returning(Future {
        None
      })
    val ldapAccountService = new AccountServiceImpl(ldapClient, accountRepository, null)
    ldapAccountService.login(username, wrongPassword) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "return nothing if a user is not found" in {
    val (wrongUsername, password) = ("user", "password")
    val ldapClient = mock[LDAPClient]
    val accountRepository = mock[AccountRepository]

    (ldapClient.findUser _)
      .expects(wrongUsername, password)
      .returning(Future {
        None
      })
    val ldapAccountService = new AccountServiceImpl(ldapClient, accountRepository, null)
    ldapAccountService.login(wrongUsername, password) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "generate a token" in {
    val secret = "abc"
    val user = User("Dude Doe", "username")
    val accountRepository = mock[AccountRepository]

    val configuration = mock[Config]
    (configuration.getString _).expects("rest.secret").returning(secret)

    val lDAPAccountService = new AccountServiceImpl(null, accountRepository, configuration)
    lDAPAccountService.refresh(user).map { token =>
      val accessToken = JwtCirce.decodeJson(token.accessToken, secret, Seq(JwtAlgorithm.HS512))
      accessToken.toOption shouldBe defined
      accessToken.get.as[Map[String, String]] should be (Right(Map(
        "name" -> user.name,
        "username" -> user.username
      )))

      val refreshToken = JwtCirce.decodeJson(token.refreshToken, secret, Seq(JwtAlgorithm.HS512))
      refreshToken.toOption shouldBe defined
      refreshToken.get.as[Map[String, String]] should be (Right(Map(
        "username" -> user.username
      )))
    }
  }

  it should "validate a valid token" in {
    val secret = "1yR2kSbv$gE@xkQRdlTtuecW"
    val user = User("Dude Doe", "username")
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicm9sZSI6InVzZXIifQ.ArnfmWsJCLLoqGR2jKWQGJAf5kxP7Um2njCykIM5XXQ"
    val accountRepository = mock[AccountRepository]

    val configuration = mock[Config]
    (configuration.getString _).expects("rest.secret").returning(secret)

    val lDAPAccountService = new AccountServiceImpl(null, accountRepository, configuration)
    lDAPAccountService.validate(token).map { maybeUser =>
      maybeUser shouldBe defined
      maybeUser.get should be (user)
    }
  }

}
