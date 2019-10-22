package io.phdata.services

import java.net.InetAddress

import cats.data._
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.phdata.AppContext
import io.phdata.models.{DistinguishedName, MemberRoleRequest, WorkspaceRequest}
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
      workspace <- workspaceService.findById(workspaceId)
      fromAddress = context.appConfig.smtp.fromEmail
      to <- context.lookupLDAPClient.findUserByDN(memberRoleRequest.distinguishedName)
      toAddress <- OptionT(Effect[F].pure(to.email))
      values = Map(
        "roleName" -> memberRoleRequest.role.get.show,
        "resourceType" -> memberRoleRequest.resource,
        "workspaceName" -> workspace.name,
        "uiUrl" -> resolveUiUrl,
        "workspaceId" -> workspaceId
      )
      email <- OptionT.liftF(Effect[F].delay(templateEngine.layout("/templates/emails/welcome.mustache", values)))
      result <- OptionT.liftF(
        context.emailClient.send(s"Archway Workspace: Welcome to ${workspace.name}", email, fromAddress, toAddress)
      )
    } yield result

  override def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit] = {
    val values = Map(
      "uiUrl" -> resolveUiUrl,
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

  private def resolveUiUrl = {
    if (context.appConfig.ui.url.isEmpty) {
      s"${InetAddress.getLocalHost.getCanonicalHostName}:${context.appConfig.rest.port}"
    } else {
      context.appConfig.ui.url
    }
  }
}
