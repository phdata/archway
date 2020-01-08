package io.phdata.services

import java.net.InetAddress

import cats.data._
import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import courier.Multipart
import io.phdata.AppContext
import io.phdata.clients.LDAPUser
import io.phdata.models.{DistinguishedName, MemberRoleRequest, WorkspaceRequest}
import org.fusesource.scalate.TemplateEngine

trait EmailService[F[_]] {

  def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit]

  def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit]
}

class EmailServiceImpl[F[_]: Effect](context: AppContext[F], workspaceService: WorkspaceService[F])
    extends EmailService[F] with LazyLogging {

  lazy val templateEngine: TemplateEngine = new TemplateEngine()

  override def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit] = {

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
        context.emailClient.send(
          s"Archway Workspace: Welcome to ${workspace.name}",
          EmbeddedImageEmail
            .create(email, List(("images/logo_big.png", "logo"), ("images/check_mark.png", "checkMark"))),
          fromAddress,
          toAddress
        )
      )
    } yield result
  }

  override def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit] = {
    val values = Map(
      "uiUrl" -> resolveUiUrl,
      "workspaceId" -> workspaceRequest.id.get,
      "workspaceName" -> workspaceRequest.name
    )

    for {
      user <- context.lookupLDAPClient.findUserByDN(workspaceRequest.requestedBy).value
      email <- Effect[F].delay(
        templateEngine.layout(
          "/templates/emails/incoming.mustache",
          values + ("userName" -> s"${user.getOrElse(LDAPUser("Unknown", "Unknown", DistinguishedName("cn=Unknown"), Seq.empty, None)).name}")
        )
      )
      addressList = context.appConfig.approvers.notificationEmail
      fromAddress = context.appConfig.smtp.fromEmail
    } yield {
      addressList.map(
        recipient =>
          context.emailClient.send(
            "A New Workspace is Waiting",
            EmbeddedImageEmail.create(email, List(("images/logo_big.png", "logo"))),
            fromAddress,
            recipient
          )
      )
    }
  }

  private def resolveUiUrl = {
    if (context.appConfig.ui.url.isEmpty) {
      s"https://${InetAddress.getLocalHost.getCanonicalHostName}:${context.appConfig.rest.port}"
    } else {
      context.appConfig.ui.url
    }
  }
}
