package com.heimdali.services

import com.heimdali.models._

trait TemplateService[F[_]] {

  def defaults(user: User): F[TemplateRequest]

  def workspaceFor(template: TemplateRequest, templateName: String): F[WorkspaceRequest]

  def customTemplates: F[List[WorkspaceRequest]]

}
