package io.phdata.repositories

import doobie.free.connection.ConnectionIO
import io.phdata.models.CustomLinkGroup

trait CustomLinkGroupRepository {

  def create(customLinkGroup: CustomLinkGroup): ConnectionIO[Long]

  def update(customLinkGroupId: Long, customLinkGroup: CustomLinkGroup): ConnectionIO[Int]

  def list: ConnectionIO[List[CustomLinkGroup]]

  def delete(customLinkGroupId: Long): ConnectionIO[Unit]
}
