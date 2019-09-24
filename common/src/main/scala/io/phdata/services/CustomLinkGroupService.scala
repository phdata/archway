package io.phdata.services

import io.phdata.models.CustomLinkGroup

trait CustomLinkGroupService[F[_]] {

  def createCustomLinkGroup(customLinkGroup: CustomLinkGroup): F[Long]

  def updateCustomLinkGroup(customLinkGroupId: Long, customLinkGroup: CustomLinkGroup): F[Unit]

  def list: F[List[CustomLinkGroup]]

  def deleteCustomLinkGroup(customLinkGroupId: Long): F[Unit]
}
