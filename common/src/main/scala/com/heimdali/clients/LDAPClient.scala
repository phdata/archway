package com.heimdali.clients

import cats.data.OptionT
import com.heimdali.services.MemberSearchResult
import com.unboundid.ldap.sdk._

sealed trait GroupCreationError

case object GroupAlreadyExists extends GroupCreationError

case class GeneralError(throwable: Throwable) extends GroupCreationError

case class LDAPUser(name: String, username: String, distinguishedName: String, memberships: Seq[String], email: Option[String])

trait LDAPClient[F[_]] {
  def findUser(distinguishedName: String): OptionT[F, LDAPUser]

  def validateUser(username: String, password: String): OptionT[F, LDAPUser]

  def createGroup(groupName: String, attributes: List[(String, String)]): F[Unit]

  def addUser(groupName: String, distinguishedName: String): OptionT[F, String]

  def removeUser(groupName: String, distinguishedName: String): OptionT[F, String]

  def groupMembers(groupDN: String): F[List[LDAPUser]]

  def search(filter: String): F[MemberSearchResult]

  def deleteGroup(groupDN: String): OptionT[F, String]
}
