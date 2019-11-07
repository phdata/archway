package io.phdata.services

import cats.data._
import cats.effect._
import cats.implicits._
import io.phdata.AppContext
import io.phdata.clients.LDAPUser
import io.phdata.models.{DistinguishedName, Manager, MemberRoleRequest}
import io.phdata.test.fixtures._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class EmailServiceImplSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  behavior of "Email Service"

  it should "send a welcome email" in new Context {
    val newMember = DistinguishedName(s"cn=username,${appConfig.ldap.userPath.get}")

    (workspaceService.findById _).expects(id).returning(OptionT.some[IO](savedWorkspaceRequest))
    (context.lookupLDAPClient.findUserByDN _).expects(newMember).returning(OptionT.some[IO](LDAPUser(personName, "username", newMember, Seq.empty, Some("username@phdata.io"))))
    (context.emailClient.send _).expects(s"Archway Workspace: Welcome to ${name}", *, appConfig.smtp.fromEmail, "username@phdata.io").returning(IO.unit)

    emailService.newMemberEmail(id, MemberRoleRequest(newMember, "data", id, Some(Manager))).value.unsafeRunSync()
  }

  it should "send a new workspace email to all recipients" in new Context {
    (context.lookupLDAPClient.findUserByDN _).expects(savedWorkspaceRequest.requestedBy)
      .returning(OptionT.fromOption[IO](LDAPUser("John Doe", "john.doe", savedWorkspaceRequest.requestedBy, Seq.empty, None).some))
    (context.emailClient.send _).expects(s"A New Workspace Is Waiting", *, appConfig.smtp.fromEmail, appConfig.approvers.notificationEmail(0)).returning(IO.unit)
    (context.emailClient.send _).expects(s"A New Workspace Is Waiting", *, appConfig.smtp.fromEmail, appConfig.approvers.notificationEmail(1)).returning(IO.unit)

    emailService.newWorkspaceEmail(savedWorkspaceRequest).unsafeRunSync()
  }

  trait Context {

    val context: AppContext[IO] = genMockContext()
    val workspaceService: WorkspaceService[IO] = mock[WorkspaceService[IO]]

    val emailService = new EmailServiceImpl[IO](context, workspaceService)

  }

}
