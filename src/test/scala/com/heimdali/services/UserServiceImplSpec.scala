package com.heimdali.services

import cats.data.OptionT
import cats.effect.IO
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.config.{ApprovalConfig, RestConfig}
import com.heimdali.models.User
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}
import io.circe.Json
import io.circe.syntax._

class UserServiceImplSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "LDAPAccountService"

  it should "return a user if one is found and password matches" in {
    val (name, username, password) = ("name", "username", "password")

    val ldapUser = LDAPUser(name, username, Seq.empty)
    val restConfig = RestConfig(123, "abc123")
    val approvalConfig = ApprovalConfig("", "")

    val ldapClient = mock[LDAPClient[IO]]
    ldapClient.validateUser _ expects(username, password) returning OptionT.some(ldapUser)
    val ldapAccountService = new AccountServiceImpl[IO](ldapClient, restConfig, approvalConfig)
    val maybeUser = ldapAccountService.login(username, password).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "return nothing if a user is found but the password doesn't match" in {
    val (name, username, actualPassword, wrongPassword) = ("name", "username", "password", "passw0rd")
    val ldapUser = LDAPUser(name, username, Seq.empty)
    val restConfig = RestConfig(123, "abc123")
    val approvalConfig = ApprovalConfig("", "")

    val ldapClient = mock[LDAPClient[IO]]
    ldapClient.validateUser _ expects(username, wrongPassword) returning OptionT.none
    val ldapAccountService = new AccountServiceImpl(ldapClient, restConfig, approvalConfig)
    val maybeUser = ldapAccountService.login(username, wrongPassword).value.unsafeRunSync()
    maybeUser should not be defined
  }

  it should "return nothing if a user is not found" in new Context {
    ldapClient.validateUser _ expects(wrongUsername, actualPassword) returning OptionT.none

    val maybeUser = ldapAccountService.login(wrongUsername, actualPassword).value.unsafeRunSync()
    maybeUser should not be defined
  }

  it should "generate a token" in new Context {
    val newToken = ldapAccountService.refresh(user).unsafeRunSync()
    val accessToken = JwtCirce.decodeJson(newToken.accessToken, secret, Seq(JwtAlgorithm.HS512))
    accessToken.toOption shouldBe defined
    accessToken.get shouldBe Json.obj(
      "name" -> user.name.asJson,
      "username" -> user.username.asJson,
      "permissions" -> Json.obj(
        "risk_management" -> false.asJson,
        "platform_operations" -> false.asJson
      )
    )

    val refreshToken = JwtCirce.decodeJson(newToken.refreshToken, secret, Seq(JwtAlgorithm.HS512))
    refreshToken.toOption shouldBe defined
    refreshToken.get shouldBe Json.obj(
      "name" -> user.name.asJson,
      "username" -> user.username.asJson,
      "permissions" -> Json.obj(
        "risk_management" -> false.asJson,
        "platform_operations" -> false.asJson
      )
    )
  }

  it should "validate a valid token" in new Context {
    ldapClient.findUser _ expects username returning OptionT.some(ldapUser)
    val maybeUser = ldapAccountService.validate(token).value.unsafeRunSync()
    maybeUser.isRight shouldBe true
    maybeUser.right.get should be(user)
  }

  it should "returns approver roles" in new Context {
    ldapClient.validateUser _ expects(username, actualPassword) returning OptionT.some(ldapUser)

    val maybeUser = ldapAccountService.login(username, actualPassword).value.unsafeRunSync()

    maybeUser shouldBe defined
  }

  trait Context {
    val (name, wrongUsername, username, actualPassword, wrongPassword) = ("Dude Doe", "user", "username", "password", "passw0rd")
    val user = User("Dude Doe", "username")
    val secret = "abc"
    val restConfig = RestConfig(123, secret)
    val approvalConfig = ApprovalConfig("", "")
    val ldapUser = LDAPUser(name, username, Seq("cn=admins,dc=jotunn,dc=io"))
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6ZmFsc2V9fQ.ltGXxBh4S7gwmIbcKz22IFWpGI2-zxad2XYOoxuGm734L8GlzfwvLRWIs-ZVKn7T8w3RJy5bKZWZoPj8951Qug"

    val ldapClient = mock[LDAPClient[IO]]

    def ldapAccountService = new AccountServiceImpl(ldapClient, restConfig, approvalConfig)
  }

}
