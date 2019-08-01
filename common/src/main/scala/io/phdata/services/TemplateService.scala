package io.phdata.services

import io.phdata.models.{TemplateRequest, User, WorkspaceRequest}

trait TemplateService[F[_]] {

  def defaults(user: User): F[TemplateRequest]

  def workspaceFor(template: TemplateRequest, templateName: String): F[WorkspaceRequest]

  def customTemplates: F[List[WorkspaceRequest]]

}
