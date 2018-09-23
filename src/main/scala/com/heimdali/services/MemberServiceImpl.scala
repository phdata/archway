package com.heimdali.services

import cats.data.OptionT
import cats.effect.Effect
import cats.implicits._
import com.heimdali.clients.LDAPClient
import com.heimdali.models._
import com.heimdali.repositories.{LDAPRepository, MemberRepository, MemberRightsRecord}
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import doobie.util.transactor.Transactor


class MemberServiceImpl[F[_]](memberRepository: MemberRepository,
                              transactor: Transactor[F],
                              ldapRepository: LDAPRepository,
                              ldapClient: LDAPClient[F])(implicit val F: Effect[F])
  extends MemberService[F]
    with LazyLogging {

  def toRight(memberRightsRecord: MemberRightsRecord): MemberRights =
    MemberRights(
      memberRightsRecord.name,
      memberRightsRecord.id,
      memberRightsRecord.role)

  def convertRecord(memberRightsRecord: List[MemberRightsRecord]): F[List[WorkspaceMemberEntry]] =
    memberRightsRecord
      .groupBy(_.username)
      .map { e =>
        ldapClient.findUser(e._1).map { user =>
            WorkspaceMemberEntry(
              e._1,
              user.name,
              user.email,
              e._2.filter(_.resource == "data").map(toRight),
              e._2.filter(_.resource == "processing").map(toRight),
              e._2.filter(_.resource == "topics").map(toRight),
              e._2.filter(_.resource == "applications").map(toRight))
        }
      }.toList.traverse(_.value).map(_.flatten)

  def members(id: Long): F[List[WorkspaceMemberEntry]] =
    memberRepository.list(id).transact(transactor).flatMap(convertRecord)

  def addMember(id: Long, memberRequest: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry] =
    for {
      registration <- OptionT(
        ldapRepository
          .find(memberRequest.resource, memberRequest.resourceId, memberRequest.role.show)
          .value
          .transact(transactor)
      )

      _ <- OptionT.some[F](logger.info(s"adding ${memberRequest.username} to ${registration.commonName} in db"))

      memberId <- OptionT.liftF(
        memberRepository.create(memberRequest.username, registration.id.get).transact(transactor)
      )

      _ <- OptionT.some[F](logger.info(s"adding ${memberRequest.username} to ${registration.commonName} in ldap"))
      _ <- ldapClient.addUser(registration.distinguishedName, memberRequest.username)
      _ <- OptionT.some[F](logger.info(s"completing ${memberRequest.username}"))
      member <- OptionT.liftF(
        (memberRepository.complete(registration.id.get, memberRequest.username), memberRepository.get(memberId))
          .mapN((_, member) => member)
          .transact(transactor) // run the complete and get in the same transaction
      )
      result <- OptionT.liftF(convertRecord(member))
    } yield result.head

  def removeMember(id: Long, memberRequest: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry] =
    for {
      registration <- OptionT(
        ldapRepository
          .find(memberRequest.resource, memberRequest.resourceId, memberRequest.role.show)
          .value
          .transact(transactor)
      )
      member <- OptionT.liftF(
        memberRepository
          .find(id, memberRequest.username)
          .transact(transactor)
      )
      _ <- ldapClient.removeUser(registration.distinguishedName, memberRequest.username)
      _ <- OptionT.liftF(
        memberRepository.delete(registration.id.get, memberRequest.username).transact(transactor)
      )
      result <- OptionT.liftF(convertRecord(member))
    } yield result.head

}
