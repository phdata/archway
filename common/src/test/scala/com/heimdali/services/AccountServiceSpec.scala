package com.heimdali.services

import cats.data._
import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.config.{ApprovalConfig, WorkspaceConfig, WorkspaceConfigItem}
import com.heimdali.generators.{DefaultApplicationGenerator, DefaultLDAPGroupGenerator, DefaultTopicGenerator}
import com.heimdali.models.TemplateRequest
import com.heimdali.provisioning.{Message, SimpleMessage}
import com.heimdali.test.fixtures._
import io.circe.Json
import io.circe.syntax._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.ExecutionContext

class AccountServiceSpec extends FlatSpec with MockFactory with Matchers with AppContextProvider {

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
    context.lookupLDAPClient.validateUser _ expects(username, actualPassword) returning OptionT.some(ldapUser)

    val maybeUser = accountService.login(username, actualPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "return nothing if a user is found but the password doesn't match" in new Context {
    context.lookupLDAPClient.validateUser _ expects(username, wrongPassword) returning OptionT.none

    val maybeUser = accountService.login(username, wrongPassword).value.unsafeRunSync()
    maybeUser should not be defined
  }

  it should "return nothing if a user is not found" in new Context {
    context.lookupLDAPClient.validateUser _ expects(wrongUsername, actualPassword) returning OptionT.none

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
    context.lookupLDAPClient.findUser _ expects standardUserDN returning OptionT.some(ldapUser)
    val maybeUser = accountService.validate(infraApproverToken).value.unsafeRunSync()
    maybeUser.isRight shouldBe true
    maybeUser.right.get should be(infraApproverUser)
  }

  it should "returns approver roles" in new Context {
    context.lookupLDAPClient.validateUser _ expects(username, actualPassword) returning OptionT.some(ldapUser)

    val maybeUser = accountService.login(username, actualPassword).value.unsafeRunSync()

    maybeUser shouldBe defined
  }

  it should "return a workspace if one exists" in new Context {
    workspaceService.findByUsername _ expects standardUsername returning OptionT.some(savedWorkspaceRequest)

    val maybeWorkspace = accountService.getWorkspace(standardUsername).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  it should "save and create a workspace" in new Context {
    val templateRequest = TemplateRequest(infraApproverUser.username, infraApproverUser.username, infraApproverUser.username, initialCompliance, infraApproverUser.distinguishedName)
    templateService.defaults _ expects infraApproverUser returning templateRequest.pure[IO]
    templateService.workspaceFor _ expects(templateRequest, "user") returning initialWorkspaceRequest.pure[IO]
    workspaceService.findByUsername _ expects standardUsername returning OptionT.none
    workspaceService.create _ expects initialWorkspaceRequest returning IO.pure(savedWorkspaceRequest)
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)

    provisioningService.attemptProvision _ expects(savedWorkspaceRequest, 0) returning NonEmptyList.one(SimpleMessage(1l, "").asInstanceOf[Message]).pure[IO].start(contextShift)

    val maybeWorkspace = accountService.createWorkspace(infraApproverUser).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  trait Context {
    implicit val timer: Timer[IO] = testTimer
    val (name, wrongUsername, username, actualPassword, wrongPassword) = ("Dude Doe", "user", "username", "password", "passw0rd")
    val secret = "abc"
    val ldapUser = LDAPUser(personName, standardUsername, standardUserDN, Seq("cn=foo,dc=jotunn,dc=io"), Some("dude@email.com"))
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6ZmFsc2V9fQ.ltGXxBh4S7gwmIbcKz22IFWpGI2-zxad2XYOoxuGm734L8GlzfwvLRWIs-ZVKn7T8w3RJy5bKZWZoPj8951Qug"

    val workspaceService = mock[WorkspaceService[IO]]
    val provisioningService = mock[ProvisioningService[IO]]
    val templateService = mock[TemplateService[IO]]

    val context: AppContext[IO] = genMockContext(
      appConfig = appConfig.copy(
        approvers = ApprovalConfig("me@meail.com", Some("CN=foo,DC=jotunN,dc=io"), Some("cN=bar,dc=JOTUNN,dc=io"))
      )
    )

    lazy val accountService = new AccountServiceImpl[IO](context, workspaceService, templateService, provisioningService)
  }

}
