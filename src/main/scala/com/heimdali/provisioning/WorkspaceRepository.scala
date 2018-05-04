package com.heimdali.provisioning

import com.heimdali.models.Workspace

import scala.concurrent.Future

trait WorkspaceRepository[T <: Workspace] {
  def setLDAP(id: String, ldapRegistrationId: Long): Future[T]

  def setHive(id: String, hiveDatabaseId: Long): Future[T]
}
