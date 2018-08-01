package com.heimdali.services

import cats.data.OptionT
import com.heimdali.models.{MemberRoleRequest, WorkspaceMemberEntry}

trait MemberService[F[_]] {

  def members(id: Long): F[List[WorkspaceMemberEntry]]

  def addMember(id: Long, request: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry]

  def removeMember(id: Long, request: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry]

}