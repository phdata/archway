package com.heimdali.services

import akka.actor.ActorRef
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.models.UserWorkspace
import com.heimdali.repositories.UserWorkspaceRepository
import com.typesafe.config.{Config, ConfigFactory}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.Future

class UserServiceImplSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "LDAPAccountService"

  it should "return a user if one is found and password matches" in {
    val (name, username, password) = ("name", "username", "password")
    val ldapClient = mock[LDAPClient]
    val accountRepository = mock[UserWorkspaceRepository]
    val factory = mockFunction[UserWorkspace, ActorRef]

    val ldapUser = LDAPUser(name, username, Seq.empty)

    (ldapClient.validateUser _)
      .expects(username, password)
      .returning(Future {
        Some(ldapUser)
      })
    val ldapAccountService = new AccountServiceImpl(ldapClient, accountRepository, ConfigFactory.load(), factory)
    ldapAccountService.login(username, password) map { maybeUser =>
      maybeUser shouldBe defined
    }
  }

  it should "return nothing if a user is found but the password doesn't match" in {
    val (name, username, actualPassword, wrongPassword) = ("name", "username", "password", "passw0rd")
    val ldapClient = mock[LDAPClient]
    val accountRepository = mock[UserWorkspaceRepository]
    val factory = mockFunction[UserWorkspace, ActorRef]

    val ldapUser = LDAPUser(name, username, Seq.empty)

    (ldapClient.validateUser _)
      .expects(username, wrongPassword)
      .returning(Future {
        None
      })
    val ldapAccountService = new AccountServiceImpl(ldapClient, accountRepository, null, factory)
    ldapAccountService.login(username, wrongPassword) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "return nothing if a user is not found" in {
    val (wrongUsername, password) = ("user", "password")
    val ldapClient = mock[LDAPClient]
    val accountRepository = mock[UserWorkspaceRepository]
    val factory = mockFunction[UserWorkspace, ActorRef]

    (ldapClient.validateUser _)
      .expects(wrongUsername, password)
      .returning(Future {
        None
      })
    val ldapAccountService = new AccountServiceImpl(ldapClient, accountRepository, null, factory)
    ldapAccountService.login(wrongUsername, password) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "generate a token" in {
    val secret = "abc"
    val user = User("Dude Doe", "username")
    val accountRepository = mock[UserWorkspaceRepository]
    val factory = mockFunction[UserWorkspace, ActorRef]

    val configuration = mock[Config]
    (configuration.getString _).expects("rest.secret").returning(secret)

    val lDAPAccountService = new AccountServiceImpl(null, accountRepository, configuration, factory)
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
    val secret = "abc"
    val user = User("Dude Doe", "username")
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIn0.B0UjtU-USy1F7LdouoiAYUmQd1VO9nbtHntNc9y3dXzkTtHd2CvCt4rDmqSNvRKdns-MJOQYPDffu1nwMPvc-A"
    val accountRepository = mock[UserWorkspaceRepository]
    val factory = mockFunction[UserWorkspace, ActorRef]

    val configuration = mock[Config]
    (configuration.getString _).expects("rest.secret").returning(secret)

    val lDAPAccountService = new AccountServiceImpl(null, accountRepository, configuration, factory)
    lDAPAccountService.validate(token).map { maybeUser =>
      maybeUser shouldBe defined
      maybeUser.get should be (user)
    }
  }

}
