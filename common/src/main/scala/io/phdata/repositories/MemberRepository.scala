package io.phdata.repositories

import io.phdata.models.{DatabaseRole, DistinguishedName, LDAPRegistration}
import doobie._

trait MemberRepository {
  def create(distinguishedName: String, ldapRegistrationId: Long): ConnectionIO[Long]

  def complete(id: Long, distinguishedName: String): ConnectionIO[Int]

  def get(id: Long): ConnectionIO[List[MemberRightsRecord]]

  def find(workspaceRequestId: Long, distinguishedName: DistinguishedName): ConnectionIO[List[MemberRightsRecord]]

  def delete(ldapRegistrationId: Long, distinguishedName: String): ConnectionIO[Int]

  def list(workspaceId: Long): ConnectionIO[List[MemberRightsRecord]]

  def listLDAPRegistrations(): ConnectionIO[List[LDAPRegistration]]

  def groupMembers: ConnectionIO[Seq[Group]]
}

case class MemberRightsRecord(resource: String, distinguishedName: String, name: String, id: Long, role: DatabaseRole)

case class GroupMembership(groupDN: String, userDN: String)

case class Group(groupDN: DistinguishedName, userDNs: List[DistinguishedName])
