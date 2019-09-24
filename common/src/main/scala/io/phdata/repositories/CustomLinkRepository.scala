package io.phdata.repositories

import doobie.free.connection.ConnectionIO
import io.phdata.models.CustomLink

trait CustomLinkRepository {

  def create(customLinkGroupId: Long, customLink: CustomLink): ConnectionIO[Long]

  def update(customLink: CustomLink): ConnectionIO[Int]

  def delete(customLinkId: Long): ConnectionIO[Unit]

  def findByCustomLinkGroupId(customLinkGroupId: Long): ConnectionIO[List[CustomLink]]
}
