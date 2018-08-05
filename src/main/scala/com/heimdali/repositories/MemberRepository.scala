package com.heimdali.repositories

import doobie._

trait MemberRepository {
  def create(username: String, ldapRegistrationId: Long): ConnectionIO[Long]

  def complete(id: Long, username: String): ConnectionIO[Int]

  def get(id: Long): ConnectionIO[List[MemberRightsRecord]]

  def find(workspaceRequestId: Long, username: String): ConnectionIO[List[MemberRightsRecord]]

  def delete(ldapRegistrationId: Long, username: String): ConnectionIO[Int]

  def list(workspaceId: Long): ConnectionIO[List[MemberRightsRecord]]
}

case class MemberRightsRecord(resource: String, username: String, name: String, id: Long, role: DatabaseRole)