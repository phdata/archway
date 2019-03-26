package com.heimdali.services

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.{EmailClient, LDAPClient}
import com.heimdali.config.AppConfig
import com.heimdali.models.{MemberRoleRequest, WorkspaceRequest}
import com.typesafe.scalalogging.LazyLogging
import org.fusesource.scalate.support.FileTemplateSource
import org.fusesource.scalate.{TemplateEngine, TemplateSource}

import scala.concurrent.ExecutionContext
import scala.io.Source

trait EmailService[F[_]] {

  def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit]

  def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit]

}

class EmailServiceImpl[F[_] : Effect](emailClient: EmailClient[F],
                                      appConfig: AppConfig,
                                      workspaceService: WorkspaceService[F],
                                      ldapClient: LDAPClient[F])
  extends EmailService[F] with LazyLogging {

  lazy val templateEngine: TemplateEngine = new TemplateEngine()

  override def newMemberEmail(workspaceId: Long, memberRoleRequest: MemberRoleRequest): OptionT[F, Unit] =
    for {
      workspace <- workspaceService.find(workspaceId)
      fromAddress = appConfig.smtp.fromEmail
      to <- ldapClient.findUser(memberRoleRequest.distinguishedName)
      toAddress <- OptionT(Effect[F].pure(to.email))
      values = Map(
        "roleName" -> memberRoleRequest.role.get.show,
        "resourceType" -> memberRoleRequest.resource,
        "workspaceName" -> workspace.name,
        "uiUrl" -> appConfig.ui.url,
        "workspaceId" -> workspaceId
      )
      email <- OptionT.liftF(Effect[F].delay(templateEngine.layout("/templates/emails/welcome.mustache", values)))
      result <- OptionT.liftF(emailClient.send(s"Welcome to ${workspace.name}", email, fromAddress, toAddress))
    } yield result

  override def newWorkspaceEmail(workspaceRequest: WorkspaceRequest): F[Unit] = {
    val values = Map(
      "uiUrl" -> appConfig.ui.url,
      "workspaceId" -> workspaceRequest.id.get
    )

    for {
      email <- Effect[F].delay(templateEngine.layout("/templates/emails/incoming.mustache", values))
      toAddress = appConfig.approvers.notificationEmail
      fromAddress = appConfig.smtp.fromEmail
      result <- emailClient.send("A New Workspace Is Waiting", email, fromAddress, toAddress)
    } yield result
  }
}