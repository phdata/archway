package com.heimdali.services

import cats.implicits._
import cats.data.OptionT
import cats.effect.Effect
import com.heimdali.clients.LDAPClient
import com.heimdali.models.WorkspaceMember
import com.heimdali.repositories.{ DatabaseRole, LDAPRepository, MemberRepository }
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.transactor.Transactor


class MemberServiceImpl[F[_]](
  memberRepository: MemberRepository,
  transactor: Transactor[F],
  ldapRepository: LDAPRepository,
  ldapClient: LDAPClient[F],
  )(implicit val F: Effect[F])
    extends MemberService[F]
    with LazyLogging {

  def members[A <: DatabaseRole](
      id: Long,
      databaseName: String,
      roleName: A
  ): F[List[WorkspaceMember]] =
    memberRepository.findByDatabase(databaseName, roleName).transact(transactor)

  def addMember(id: Long, username: String): F[Long] =
    memberRepository.create(username, id).transact(transactor)

  def addMember[A <: DatabaseRole](
      id: Long,
      databaseName: String,
      roleName: A,
      username: String
  ): OptionT[F, WorkspaceMember] =
    for {
      _ <- OptionT.pure[F](logger.info("looking up info for request id {} db {} and role {}", id, databaseName, roleName))
      registration <- OptionT(
        ldapRepository
          .find(id, databaseName, roleName)
          .value
          .transact(transactor)
      )
      _ <- OptionT.some[F](logger.info(s"adding $username to ${registration.commonName} in db"))
      memberId <- OptionT.liftF(
        memberRepository.create(username, registration.id.get).transact(transactor)
      )
      _ <- OptionT.some[F](logger.info(s"adding $username to ${registration.commonName} in ldap"))
      _ <- ldapClient.addUser(registration.distinguishedName, username)
      _ <- OptionT.some[F](logger.info(s"completing $username"))
      member <- OptionT.liftF(
        (memberRepository.complete(memberId), memberRepository.get(memberId))
          .mapN((_, member) => member)
          .transact(transactor) // run the complete and get in the same transaction
      )
    } yield member

  def removeMember[A <: DatabaseRole](
      id: Long,
      databaseName: String,
      roleName: A,
      username: String
  ): OptionT[F, WorkspaceMember] =
    for {
      registration <- OptionT(
        ldapRepository
          .find(id, databaseName, roleName)
          .value
          .transact(transactor)
      )
      member <- OptionT(
        memberRepository
          .find(registration.id.get, username)
          .value
          .transact(transactor)
      )
      _ <- ldapClient.removeUser(registration.commonName, username)
      _ <- OptionT.liftF(
        memberRepository.delete(member.id.get).transact(transactor)
      )
    } yield member

}
