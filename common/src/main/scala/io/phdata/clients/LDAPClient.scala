package io.phdata.clients

import cats.data.OptionT
import io.phdata.config.Password
import io.phdata.models.DistinguishedName
import io.phdata.services.MemberSearchResult

sealed trait GroupCreationError

case object GroupAlreadyExists extends GroupCreationError

case class GeneralError(throwable: Throwable) extends GroupCreationError

case class LDAPUser(
    name: String,
    username: String,
    distinguishedName: DistinguishedName,
    memberships: Seq[String],
    email: Option[String]
)

trait LDAPClient[F[_]] {
  def findUserByDN(distinguishedName: DistinguishedName): OptionT[F, LDAPUser]

  def findUserByUserName(username: String): OptionT[F, LDAPUser]

  def validateUser(username: String, password: Password): OptionT[F, LDAPUser]

  def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit]

  def deleteGroup(groupDN: DistinguishedName): OptionT[F, String]

  def addUserToGroup(groupName: DistinguishedName, distinguishedName: DistinguishedName): OptionT[F, String]

  def removeUserFromGroup(groupName: DistinguishedName, distinguishedName: DistinguishedName): OptionT[F, String]

  def groupMembers(groupDN: DistinguishedName): F[List[LDAPUser]]

  def search(filter: String): F[MemberSearchResult]
}
