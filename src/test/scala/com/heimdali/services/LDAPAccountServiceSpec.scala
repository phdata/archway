package com.heimdali.services

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class LDAPAccountServiceSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "LDAPAccountService"

  it should "return a user if one is found and password matches" in {
    val (name, username, password) = ("name", "username", "password")
    val ldapClient = mock[LDAPClient]

    val ldapUser = LDAPUser(name, username, password, Seq.empty)

    (ldapClient.findUser _)
      .expects(username)
      .returning(Future {Some(ldapUser)})
    val ldapAccountService = new LDAPAccountService(ldapClient)
    ldapAccountService.login(username, password) map { maybeUser =>
      maybeUser shouldBe defined
      maybeUser.get.username should be (username)
    }
  }

  it should "return nothing if a user is found but the password doesn't match" in {
    val (name, username, actualPassword, wrongPassword) = ("name", "username", "password", "passw0rd")
    val ldapClient = mock[LDAPClient]

    val ldapUser = LDAPUser(name, username, actualPassword, Seq.empty)

    (ldapClient.findUser _)
      .expects(username)
      .returning(Future {Some(ldapUser)})
    val ldapAccountService = new LDAPAccountService(ldapClient)
    ldapAccountService.login(username, wrongPassword) map { maybeUser =>
      maybeUser should not be defined
    }
  }

  it should "return nothing if a user is not found" in {
    val (wrongUsername, password) = ("user", "password")
    val ldapClient = mock[LDAPClient]

    (ldapClient.findUser _)
      .expects(wrongUsername)
      .returning(Future {None})
    val ldapAccountService = new LDAPAccountService(ldapClient)
    ldapAccountService.login(wrongUsername, password) map { maybeUser =>
      maybeUser should not be defined
    }
  }

}
