package com.heimdali.services

import cats.data._
import cats.effect._
import com.heimdali.AppContext
import com.heimdali.clients.LDAPUser
import com.heimdali.models.{Manager, MemberRoleRequest}
import com.heimdali.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class EmailServiceImplSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  behavior of "Email Service"

  it should "send a welcome email" in new Context {
    val newMember = s"cn=username,${appConfig.ldap.userPath.get}"

    (workspaceService.find _).expects(id).returning(OptionT.some[IO](savedWorkspaceRequest))
    (context.lookupLDAPClient.findUser _).expects(newMember).returning(OptionT.some[IO](LDAPUser(personName, "username", newMember, Seq.empty, Some("username@phdata.io"))))
    (context.emailClient.send _).expects(s"Welcome to $name", *, appConfig.smtp.fromEmail, "username@phdata.io").returning(IO.unit)

    emailService.newMemberEmail(id, MemberRoleRequest(newMember, "data", id, Some(Manager))).value.unsafeRunSync()
  }

  it should "send a new workspace email to all recipients" in new Context {
    (context.emailClient.send _).expects(s"A New Workspace Is Waiting", *, appConfig.smtp.fromEmail, appConfig.approvers.notificationEmail(0)).returning(IO.unit)
    (context.emailClient.send _).expects(s"A New Workspace Is Waiting", *, appConfig.smtp.fromEmail, appConfig.approvers.notificationEmail(1)).returning(IO.unit)

    emailService.newWorkspaceEmail(savedWorkspaceRequest).unsafeRunSync()
  }

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  trait Context {

    val context: AppContext[IO] = genMockContext()
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]

    val emailService = new EmailServiceImpl[IO](context, workspaceService)

  }

}
