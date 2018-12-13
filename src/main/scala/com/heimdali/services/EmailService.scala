package com.heimdali.services

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.{EmailClient, LDAPClient}
import com.heimdali.config.AppConfig
import com.heimdali.models.MemberRoleRequest
import com.typesafe.scalalogging.LazyLogging
import scalatags.Text

import scala.concurrent.ExecutionContext

trait EmailService[F[_]] {

  def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit]

}

class EmailServiceImpl[F[_]](emailClient: EmailClient[F],
                             appConfig: AppConfig,
                             workspaceService: WorkspaceService[F],
                             ldapClient: LDAPClient[F],
                            )(implicit val F: Effect[F], executionContext: ExecutionContext)
  extends EmailService[F] with LazyLogging {

  import scalatags.Text.all._

  private def welcomeEmail(memberName: String,
                           roleName: String,
                           resourceType: String,
                           workspaceName: String,
                           workspaceId: Long): Text.TypedTag[String] =
    div(
      backgroundColor := "#f0f3f5",
      fontFamily := "'Helvetica Neue', Helvetica, 'Segoe UI', Arial, sans-serif",
      fontWeight := "200")(
      div(
        color := "#ffffff",
        backgroundColor := "#2e4052",
        padding := "50px",
        textAlign := "center",
        textTransform := "uppercase")(
        div(fontSize := "24px")(s"WELCOME ABOARD, $memberName!")
      ),
      div(
        width := "50%",
        padding := "25px",
        margin := "50px auto",
        backgroundColor := "#fff")(
        p(s"Dear $memberName,"),
        p(s"You've been added as a $roleName to $resourceType in $workspaceName. Use the link below to go check it out!")
      ),
      a(backgroundColor := "#2e4052",
        textDecoration := "none",
        padding := "10px 25px",
        margin := "50px auto",
        width := "250px",
        display := "block",
        color := "#ffffff",
        fontWeight := "500",
        textAlign := "center",
        href := s"${appConfig.ui.url}/workspaces/$workspaceId")(
        "GO THERE"
      )
    )

  override def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit] =
    for {
      workspace <- workspaceService.find(workspaceId)
      from <- ldapClient.findUser(workspace.requestedBy)
      fromAddress <- OptionT(F.pure(from.email))
      to <- ldapClient.findUser(memberRoleRequest.distinguishedName)
      toAddress <- OptionT(F.pure(to.email))
      email = welcomeEmail(to.name, memberRoleRequest.role.show, memberRoleRequest.resource, workspace.name, workspaceId)
      result <- OptionT.liftF(emailClient.send(s"Welcome to ${workspace.name}", email, fromAddress, toAddress))
    } yield result

}