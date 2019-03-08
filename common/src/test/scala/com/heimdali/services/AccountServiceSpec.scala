package com.heimdali.services

import cats.data._
import cats.effect.IO
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.config.{ApprovalConfig, RestConfig, WorkspaceConfig, WorkspaceConfigItem}
import com.heimdali.models.WorkspaceRequest
import com.heimdali.generators.DefaultUserWorkspaceGenerator
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

class AccountServiceSpec extends FlatSpec with MockFactory with Matchers {

  behavior of "Account Service"

  it should "return appropriate roles" in new Context {
    val user = accountService.convertUser(ldapUser)

    user.permissions.platformOperations should be(true)
  }

  it should "handle different cases for roles" in new Context {
    val user = accountService.convertUser(ldapUser)

    user.permissions.platformOperations should be(true)
  }

  it should "return a user if one is found and password matches" in new Context {
    ldapClient.validateUser _ expects(username, actualPassword) returning OptionT.some(ldapUser)

    val maybeUser = accountService.login(username, actualPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "return nothing if a user is found but the password doesn't match" in new Context {
    ldapClient.validateUser _ expects(username, wrongPassword) returning OptionT.none

    val maybeUser = accountService.login(username, wrongPassword).value.unsafeRunSync()
    maybeUser should not be defined
  }

  it should "return nothing if a user is not found" in new Context {
    ldapClient.validateUser _ expects(wrongUsername, actualPassword) returning OptionT.none

    val maybeUser = accountService.login(wrongUsername, actualPassword).value.unsafeRunSync()
    maybeUser should not be defined
  }

  it should "generate a token" in new Context {
    val newToken = accountService.refresh(infraApproverUser).unsafeRunSync()
    val accessToken = JwtCirce.decodeJson(newToken.accessToken, secret, Seq(JwtAlgorithm.HS512))
    accessToken.toOption shouldBe defined
    accessToken.get shouldBe Json.obj(
      "name" -> infraApproverUser.name.asJson,
      "username" -> infraApproverUser.username.asJson,
      "distinguished_name" -> infraApproverUser.distinguishedName.asJson,
      "permissions" -> Json.obj(
        "risk_management" -> false.asJson,
        "platform_operations" -> true.asJson
      )
    )

    val refreshToken = JwtCirce.decodeJson(newToken.refreshToken, secret, Seq(JwtAlgorithm.HS512))
    refreshToken.toOption shouldBe defined
    refreshToken.get shouldBe Json.obj(
      "name" -> infraApproverUser.name.asJson,
      "username" -> infraApproverUser.username.asJson,
      "distinguished_name" -> infraApproverUser.distinguishedName.asJson,
      "permissions" -> Json.obj(
        "risk_management" -> false.asJson,
        "platform_operations" -> true.asJson
      )
    )
  }

  it should "validate a valid token" in new Context {
    ldapClient.findUser _ expects standardUserDN returning OptionT.some(ldapUser)
    val maybeUser = accountService.validate(infraApproverToken).value.unsafeRunSync()
    maybeUser.isRight shouldBe true
    maybeUser.right.get should be(infraApproverUser)
  }

  it should "returns approver roles" in new Context {
    ldapClient.validateUser _ expects(username, actualPassword) returning OptionT.some(ldapUser)

    val maybeUser = accountService.login(username, actualPassword).value.unsafeRunSync()

    maybeUser shouldBe defined
  }

  it should "return a workspace if one exists" in new Context {
    workspaceService.findByUsername _ expects standardUsername returning OptionT.some(savedWorkspaceRequest)

    val maybeWorkspace = accountService.getWorkspace(standardUsername).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  it should "save and create a workspace" in new Context {
    val userWorkspace: WorkspaceRequest = (for {
      service <- IO.pure(new DefaultUserWorkspaceGenerator[IO](appConfig))
      template <- service.defaults(infraApproverUser)
      result <- service.workspaceFor(template)
    } yield result.copy(requestDate = clock.instant())).unsafeRunSync()

    workspaceService.findByUsername _ expects standardUsername returning OptionT.none
    workspaceService.create _ expects userWorkspace returning IO.pure(savedWorkspaceRequest)
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)

    provisioningService.provision _ expects savedWorkspaceRequest returning IO.pure(NonEmptyList.one(""))

    val maybeWorkspace = accountService.createWorkspace(infraApproverUser).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  trait Context {
    val (name, wrongUsername, username, actualPassword, wrongPassword) = ("Dude Doe", "user", "username", "password", "passw0rd")
    val approvalConfig = ApprovalConfig("me@meail.com", "CN=foo,DC=jotunN,dc=io", "cN=bar,dc=JOTUNN,dc=io")
    val secret = "abc"
    val restConfig = RestConfig(1234, secret)
    val ldapUser = LDAPUser(personName, standardUsername, standardUserDN, Seq("cn=foo,dc=jotunn,dc=io"), Some("dude@email.com"))
    val workspaceConfigItem = WorkspaceConfigItem("root.user", 1, 1, 1, "root.user")
    val workspaceConfig = WorkspaceConfig(workspaceConfigItem, workspaceConfigItem, workspaceConfigItem)
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6ZmFsc2V9fQ.ltGXxBh4S7gwmIbcKz22IFWpGI2-zxad2XYOoxuGm734L8GlzfwvLRWIs-ZVKn7T8w3RJy5bKZWZoPj8951Qug"

    val workspaceService = mock[WorkspaceService[IO]]
    val ldapClient = mock[LDAPClient[IO]]
    val templateService = new DefaultUserWorkspaceGenerator[IO](appConfig)
    val provisioningService = mock[ProvisioningService[IO]]

    lazy val accountService = new AccountServiceImpl[IO](ldapClient, restConfig, approvalConfig, workspaceConfig, workspaceService, templateService, provisioningService)
  }

}
