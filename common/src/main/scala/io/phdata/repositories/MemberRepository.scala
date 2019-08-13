package io.phdata.repositories

import io.phdata.models.{DatabaseRole, DistinguishedName}
import doobie._

trait MemberRepository {
  def create(distinguishedName: String, ldapRegistrationId: Long): ConnectionIO[Long]

  def complete(id: Long, distinguishedName: String): ConnectionIO[Int]

  def get(id: Long): ConnectionIO[List[MemberRightsRecord]]

  def find(workspaceRequestId: Long, distinguishedName: DistinguishedName): ConnectionIO[List[MemberRightsRecord]]

  def delete(ldapRegistrationId: Long, distinguishedName: String): ConnectionIO[Int]

  def list(workspaceId: Long): ConnectionIO[List[MemberRightsRecord]]
}

case class MemberRightsRecord(resource: String, distinguishedName: String, name: String, id: Long, role: DatabaseRole)
