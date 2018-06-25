package com.heimdali.repositories

import cats.data.OptionT
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

  def delete(id: Long): ConnectionIO[Int]

  def find(registrationId: Long, username: String): OptionT[ConnectionIO, WorkspaceMember]
}
