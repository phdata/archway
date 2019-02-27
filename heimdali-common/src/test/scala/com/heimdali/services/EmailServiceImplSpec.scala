package com.heimdali.services

import cats.data._
import cats.effect._
import com.heimdali.clients.{EmailClient, LDAPClient, LDAPUser}
import com.heimdali.models.{Manager, MemberRoleRequest}
import com.heimdali.test.fixtures._
import org.fusesource.scalate.TemplateEngine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class EmailServiceImplSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "Email Service"

  it should "send a welcome email" in new Context {
    val newMember = s"cn=username,${appConfig.ldap.userPath.get}"

    (workspaceService.find _).expects(id).returning(OptionT.some[IO](savedWorkspaceRequest))
    (ldapClient.findUser _).expects(newMember).returning(OptionT.some[IO](LDAPUser(personName, "username", newMember, Seq.empty, Some("username@phdata.io"))))
    (emailClient.send _).expects(s"Welcome to $name", *, appConfig.smtp.fromEmail, "username@phdata.io").returning(IO.unit)

    emailService.newMemberEmail(id, MemberRoleRequest(newMember, "data", id, Some(Manager))).value.unsafeRunSync()
  }

  it should "send a new workspace email" in new Context {
    (emailClient.send _).expects(s"A New Workspace Is Waiting", *, appConfig.smtp.fromEmail, appConfig.approvers.notificationEmail).returning(IO.unit)

    emailService.newWorkspaceEmail(savedWorkspaceRequest).unsafeRunSync()
  }

  trait Context {

    import scala.concurrent.ExecutionContext.Implicits.global

    val emailClient = mock[EmailClient[IO]]
    val workspaceService = mock[WorkspaceService[IO]]
    val ldapClient = mock[LDAPClient[IO]]
    val templateEngine = new TemplateEngine()

    val emailService = new EmailServiceImpl[IO](emailClient, appConfig, workspaceService, ldapClient, templateEngine)

  }

}
