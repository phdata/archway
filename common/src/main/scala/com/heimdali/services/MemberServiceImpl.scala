package com.heimdali.services

import cats.data.{NonEmptyList, OptionT}
import cats.effect.Effect
import cats.implicits._
import com.heimdali.clients.LDAPClient
import com.heimdali.models._
import com.heimdali.repositories.{LDAPRepository, MemberRepository, MemberRightsRecord}
import com.typesafe.scalalogging.LazyLogging
import com.unboundid.ldap.sdk.SearchResultEntry
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor


class MemberServiceImpl[F[_]](memberRepository: MemberRepository,
                              transactor: Transactor[F],
                              ldapRepository: LDAPRepository,
                              lookupClient: LDAPClient[F],
                              provisioningClient: LDAPClient[F],
                             )(implicit val F: Effect[F])
  extends MemberService[F]
    with LazyLogging {

  def toRight(memberRightsRecord: MemberRightsRecord): MemberRights =
    MemberRights(
      memberRightsRecord.name,
      memberRightsRecord.id,
      memberRightsRecord.role)

  def convertRecord(memberRightsRecord: List[MemberRightsRecord]): F[List[WorkspaceMemberEntry]] =
    memberRightsRecord
      .groupBy(_.distinguishedName)
      .map { e =>
        lookupClient.findUser(e._1).map { user =>
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
          .find(memberRequest.resource, memberRequest.resourceId, memberRequest.role.get.show)
          .value
          .transact(transactor)
      )

      _ <- OptionT.some[F](logger.info(s"adding ${memberRequest.distinguishedName} to ${registration.commonName} in db"))

      memberId <- OptionT.liftF(
        memberRepository.create(memberRequest.distinguishedName, registration.id.get).transact(transactor)
      )

      _ <- OptionT.some[F](logger.info(s"adding ${memberRequest.distinguishedName} to ${registration.commonName} in ldap"))

      _ <- provisioningClient.addUser(registration.distinguishedName, memberRequest.distinguishedName)

      _ <- OptionT.some[F](logger.info(s"completing ${memberRequest.distinguishedName}"))

      member <- OptionT.liftF(
        (memberRepository.complete(registration.id.get, memberRequest.distinguishedName), memberRepository.get(memberId))
          .mapN((_, member) => member)
          .transact(transactor) // run the complete and get in the same transaction
      )

      result <- OptionT.liftF(convertRecord(member))

      _ <- OptionT.some[F](logger.info(result.toString()))
    } yield result.head

  def removeMember(id: Long, memberRequest: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry] =
    for {
      _ <- OptionT.some[F](logger.info(s"[REMOVING MEMBER] removing ${memberRequest.distinguishedName} from ${memberRequest.resource} in workspace ${memberRequest.resourceId}"))

      registration <- OptionT(ldapRepository.findAll(memberRequest.resource, memberRequest.resourceId).transact(transactor).map(NonEmptyList.fromList))

      _ <- OptionT.some[F](logger.info(s"[REMOVING MEMBER] found ${registration.size} ldap registration ${registration.map(_.commonName)} in db"))

      member <- OptionT.liftF(registration.map(reg => memberRepository.find(id, reg.distinguishedName)).sequence.transact(transactor).map(_.toList.flatten))

      _ <- OptionT.some[F](logger.info(s"[REMOVING MEMBER] found the following members: ${member.map(_.distinguishedName).mkString(", ")}"))

      _ <- OptionT.liftF(registration.map(reg => provisioningClient.removeUser(reg.distinguishedName, memberRequest.distinguishedName).value).sequence)

      _ <- OptionT.some[F](logger.info(s"[REMOVING MEMBER] removed ${memberRequest.distinguishedName} from ${registration.map(_.commonName)}"))

      _ <- OptionT.liftF(registration.map(reg => memberRepository.delete(reg.id.get, memberRequest.distinguishedName)).sequence.transact(transactor))

      _ <- OptionT.some[F](logger.info(s"[REMOVING MEMBER] deleted ${memberRequest.distinguishedName} record"))

      result <- OptionT(convertRecord(member).map(_.headOption))
    } yield result

  def toResult(searchResultEntry: SearchResultEntry): MemberSearchResultItem =
    MemberSearchResultItem(searchResultEntry.getAttributeValue("cn"), searchResultEntry.getDN)

  override def availableMembers(filter: String): F[MemberSearchResult] =
    lookupClient.search(filter).map { res =>
      MemberSearchResult(
        res.filter(_.getObjectClassValues.exists(_ == "user")).map(toResult),
        res.filter(_.getObjectClassValues.exists(_ == "group")).map(toResult)
      )
    }
}
