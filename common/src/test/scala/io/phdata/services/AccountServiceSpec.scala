package io.phdata.services

import cats.data._
import cats.effect.{IO, Timer}
import cats.implicits._
import io.circe.Json
import io.circe.syntax._
import io.phdata.AppContext
import io.phdata.clients.LDAPUser
import io.phdata.config.{ApprovalConfig, Password}
import io.phdata.models.{DistinguishedName, TemplateRequest}
import io.phdata.provisioning.{Message, SimpleMessage}
import io.phdata.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import pdi.jwt.{JwtAlgorithm, JwtCirce}

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

    val maybeUser = accountService.ldapAuth(username, actualPassword).value.unsafeRunSync()
    maybeUser shouldBe defined
  }

  it should "return nothing if a user is found but the password doesn't match" in new Context {
    context.lookupLDAPClient.validateUser _ expects(username, wrongPassword) returning OptionT.none

    val maybeUser = accountService.ldapAuth(username, wrongPassword).value.unsafeRunSync()
    maybeUser should not be defined
  }

  it should "return nothing if a user is not found" in new Context {
    context.lookupLDAPClient.validateUser _ expects(wrongUsername, actualPassword) returning OptionT.none

    val maybeUser = accountService.ldapAuth(wrongUsername, actualPassword).value.unsafeRunSync()
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

  it should "return a token after spnego validatation" in new Context {
    val spnegoHeader = "Negotiate fDcadfaeRadfcX"

    context.kerberosClient.spnegoUsername _ expects spnegoHeader returning EitherT.rightT(username)
    context.lookupLDAPClient.getUser _ expects username returning OptionT.some(ldapUser)

    val Right(token) = accountService.spnegoAuth(spnegoHeader).unsafeRunSync()
  }

  it should "validate a valid token" in new Context {
    context.lookupLDAPClient.findUser _ expects standardUserDN returning OptionT.some(ldapUser)
    val maybeUser = accountService.validate(infraApproverToken).value.unsafeRunSync()
    maybeUser.isRight shouldBe true
    maybeUser.right.get should be(infraApproverUser)
  }

  it should "returns approver roles" in new Context {
    context.lookupLDAPClient.validateUser _ expects(username, actualPassword) returning OptionT.some(ldapUser)

    val maybeUser = accountService.ldapAuth(username, actualPassword).value.unsafeRunSync()

    maybeUser shouldBe defined
  }

  it should "return a workspace if one exists" in new Context {
    workspaceService.findByUsername _ expects standardUsername returning OptionT.some(savedWorkspaceRequest)

    val maybeWorkspace = accountService.getWorkspace(standardUsername).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  it should "save and create a workspace" in new Context {
    val templateRequest = TemplateRequest(infraApproverUser.username, infraApproverUser.username, infraApproverUser.username, initialCompliance, DistinguishedName(infraApproverUser.distinguishedName))
    templateService.defaults _ expects infraApproverUser returning templateRequest.pure[IO]
    templateService.workspaceFor _ expects(templateRequest, "user") returning initialWorkspaceRequest.pure[IO]
    workspaceService.findByUsername _ expects standardUsername returning OptionT.none
    workspaceService.create _ expects initialWorkspaceRequest returning IO.pure(savedWorkspaceRequest)
    workspaceService.find _ expects id returning OptionT.some(savedWorkspaceRequest)

    provisioningService.attemptProvision _ expects(savedWorkspaceRequest, 0) returning NonEmptyList.one(SimpleMessage(1l, "").asInstanceOf[Message]).pure[IO].start(contextShift)

    val maybeWorkspace = accountService.createWorkspace(infraApproverUser).value.unsafeRunSync()

    maybeWorkspace shouldBe Some(savedWorkspaceRequest)
  }

  trait Context {
    implicit val timer: Timer[IO] = testTimer
    val (name, wrongUsername, username, actualPassword, wrongPassword) = ("Dude Doe", "user", "username", Password("password"), Password("passw0rd"))
    val secret = "abc"
    val ldapUser = LDAPUser(personName, standardUsername, standardUserDN.value, Seq("cn=foo,dc=jotunn,dc=io"), Some("dude@email.com"))

    val workspaceService = mock[WorkspaceService[IO]]
    val provisioningService = mock[ProvisioningService[IO]]
    val templateService = mock[TemplateService[IO]]

    val context: AppContext[IO] = genMockContext(
      appConfig = appConfig.copy(
        approvers = ApprovalConfig(Seq("me@meail.com"), Some("CN=foo,DC=jotunN,dc=io"), Some("cN=bar,dc=JOTUNN,dc=io"))
      )
    )

    lazy val accountService = new AccountServiceImpl[IO](context, workspaceService, templateService, provisioningService)
  }

}
