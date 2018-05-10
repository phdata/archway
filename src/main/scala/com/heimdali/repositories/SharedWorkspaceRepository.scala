package com.heimdali.repositories

import com.heimdali.models.SharedWorkspace
import com.heimdali.provisioning.WorkspaceRepository

import scala.concurrent.Future

trait SharedWorkspaceRepository extends WorkspaceRepository[Long, SharedWorkspace] {
  def find(id: Long): Future[Option[SharedWorkspace]]

  def list(names: Seq[String]): Future[Seq[SharedWorkspace]]

  def create(sharedWorkspace: SharedWorkspace): Future[SharedWorkspace]

  def setLDAP(workspaceId: Long, ldapRegistrationId: Long): Future[SharedWorkspace]

  def setHive(workspaceId: Long, hiveDatbaseId: Long): Future[SharedWorkspace]
}