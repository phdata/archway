package com.heimdali.repositories

import com.heimdali.models.WorkspaceMember
import doobie._

trait MemberRepository {
  def create(username: String, ldapRegistrationId: Long): ConnectionIO[Long]

  def findByDatabase(
      databaseName: String,
      role: DatabaseRole
  ): ConnectionIO[List[WorkspaceMember]]

  def complete(id: Long): ConnectionIO[Int]

  def get(id: Long): ConnectionIO[WorkspaceMember]
}
