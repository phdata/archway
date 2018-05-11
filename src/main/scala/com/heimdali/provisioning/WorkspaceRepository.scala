package com.heimdali.provisioning

import com.heimdali.models.Workspace

import scala.concurrent.Future

trait WorkspaceRepository[A, T <: Workspace[A]] {
  def setLDAP(id: A, ldapRegistrationId: Long): Future[T]

  def setHive(id: A, hiveDatabaseId: Long): Future[T]

  def setYarn(id: A, yarnId: Long): Future[T]
}
