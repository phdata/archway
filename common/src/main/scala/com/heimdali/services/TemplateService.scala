package com.heimdali.services

import com.heimdali.models._

trait TemplateService[F[_]] {

  def defaults(user: User): F[TemplateRequest]

  def workspaceFor(template: TemplateRequest, templateName: String): F[WorkspaceRequest]

}

object TemplateService {

  def generateName(name: String): String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase

}






