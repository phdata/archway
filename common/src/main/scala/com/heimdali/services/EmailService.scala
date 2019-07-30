package com.heimdali.services

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.models.{MemberRoleRequest, UserDN, WorkspaceRequest}
import com.typesafe.scalalogging.LazyLogging
import org.fusesource.scalate.TemplateEngine

trait EmailService[F[_]] {

  def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit]

  def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit]

}

class EmailServiceImpl[F[_]: Effect](context: AppContext[F], workspaceService: WorkspaceService[F])
    extends EmailService[F] with LazyLogging {

  lazy val templateEngine: TemplateEngine = new TemplateEngine()

  override def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit] =
    for {
      workspace <- workspaceService.find(workspaceId)
      fromAddress = context.appConfig.smtp.fromEmail
      to <- context.lookupLDAPClient.findUser(UserDN(memberRoleRequest.distinguishedName))
      toAddress <- OptionT(Effect[F].pure(to.email))
      values = Map(
        "roleName" -> memberRoleRequest.role.get.show,
        "resourceType" -> memberRoleRequest.resource,
        "workspaceName" -> workspace.name,
        "uiUrl" -> context.appConfig.ui.url,
        "workspaceId" -> workspaceId
      )
      email <- OptionT.liftF(Effect[F].delay(templateEngine.layout("/templates/emails/welcome.mustache", values)))
      result <- OptionT.liftF(context.emailClient.send(s"Welcome to ${workspace.name}", email, fromAddress, toAddress))
    } yield result

  override def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit] = {
    val values = Map(
      "uiUrl" -> context.appConfig.ui.url,
      "workspaceId" -> workspaceRequest.id.get
    )

    for {
      email <- Effect[F].delay(templateEngine.layout("/templates/emails/incoming.mustache", values))
      addressList = context.appConfig.approvers.notificationEmail
      fromAddress = context.appConfig.smtp.fromEmail
    } yield {
      addressList.map(
        recipient => context.emailClient.send("A New Workspace Is Waiting", email, fromAddress, recipient)
      )
    }
  }
}
