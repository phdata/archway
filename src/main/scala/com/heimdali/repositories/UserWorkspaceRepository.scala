package com.heimdali.repositories

import com.heimdali.models.UserWorkspace
import com.heimdali.provisioning.WorkspaceRepository

import scala.concurrent.Future

trait UserWorkspaceRepository extends WorkspaceRepository[String, UserWorkspace] {
  def findUser(username: String): Future[Option[UserWorkspace]]

  def create(username: String): Future[UserWorkspace]

  def setLDAP(username: String, ldapRegistrationId: Long): Future[UserWorkspace]

  def setHive(username: String, hiveDatbaseId: Long): Future[UserWorkspace]

  def setYarn(id: String, yarnId: Long): Future[UserWorkspace]
}