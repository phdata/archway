package com.heimdali.services

import cats.data.OptionT
import com.heimdali.models.WorkspaceMember
import com.heimdali.repositories.DatabaseRole

trait MemberService[F[_]] {

  def members[A <: DatabaseRole](
      id: Long,
      databaseName: String,
      roleName: A
  ): F[List[WorkspaceMember]]

  def addMember(id: Long, username: String): F[Long]

  def addMember[A <: DatabaseRole](
      id: Long,
      databaseName: String,
      roleName: A,
      username: String
  ): OptionT[F, WorkspaceMember]

  def removeMember[A <: DatabaseRole](
      id: Long,
      databaseName: String,
      roleName: A,
      username: String
  ): OptionT[F, WorkspaceMember]

}
