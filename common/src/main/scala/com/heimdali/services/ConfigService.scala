package com.heimdali.services

import com.heimdali.models.WorkspaceRequest

trait ConfigService[F[_]] {

  def getTemplate(templateName: String): F[String]

  def getAndSetNextGid: F[Long]

}


