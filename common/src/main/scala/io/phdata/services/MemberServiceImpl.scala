package io.phdata.services

import cats.data.{NonEmptyList, OptionT}
import cats.effect.Effect
import cats.implicits._
import io.phdata.AppContext
import io.phdata.models._
import io.phdata.repositories.MemberRightsRecord
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._

class MemberServiceImpl[F[_]](context: AppContext[F])(implicit val F: Effect[F])
    extends MemberService[F] with LazyLogging {

  def toRight(memberRightsRecord: MemberRightsRecord): MemberRights =
    MemberRights(memberRightsRecord.name, memberRightsRecord.id, memberRightsRecord.role)

  def convertRecord(memberRightsRecord: List[MemberRightsRecord]): F[List[WorkspaceMemberEntry]] =
    memberRightsRecord
      .groupBy(_.distinguishedName)
      .map { e =>
        context.lookupLDAPClient.findUserByDN(DistinguishedName(e._1)).map { user =>
          WorkspaceMemberEntry(
            e._1,
            user.name,
            user.email,
            e._2.filter(_.resource == "data").map(toRight),
            e._2.filter(_.resource == "processing").map(toRight),
            e._2.filter(_.resource == "topics").map(toRight),
            e._2.filter(_.resource == "applications").map(toRight)
          )
        }
      }
      .toList
      .traverse(_.value)
      .map(_.flatten)

  def members(workspaceId: Long): F[List[WorkspaceMemberEntry]] =
    context.memberRepository.list(workspaceId).transact(context.transactor).flatMap(convertRecord)

  def addMember(workspaceId: Long, memberRequest: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry] =
    for {
      registration <- OptionT(
        context.ldapRepository
          .find(memberRequest.resource, memberRequest.resourceId, memberRequest.role.get.show)
          .value
          .transact(context.transactor)
      )

      _ <- OptionT.some[F](
        logger.info(s"adding ${memberRequest.distinguishedName} to ${registration.commonName} in ldap")
      )

      user <- context.lookupLDAPClient.findUserByDN(memberRequest.distinguishedName)

      _ <- OptionT.liftF(context.hdfsClient.createUserDirectory(user.username))

      _ <- context.provisioningLDAPClient
        .addUserToGroup(registration.distinguishedName, memberRequest.distinguishedName)

      _ <- OptionT.some[F](
        logger.info(s"adding ${memberRequest.distinguishedName} to ${registration.commonName} in db")
      )

      memberId <- OptionT.liftF(
        context.memberRepository
          .create(memberRequest.distinguishedName, registration.id.get)
          .transact(context.transactor)
      )

      _ <- OptionT.some[F](logger.info(s"completing ${memberRequest.distinguishedName}"))

      member <- OptionT.liftF(
        (
          context.memberRepository.complete(registration.id.get, memberRequest.distinguishedName),
          context.memberRepository.get(memberId)
        ).mapN((_, member) => member).transact(context.transactor) // run the complete and get in the same transaction
      )

      result <- OptionT.liftF(convertRecord(member))

      _ <- OptionT.liftF(ImpalaService.invalidateMetadata(workspaceId)(context))

      _ <- OptionT.some[F](logger.info(result.toString()))
    } yield result.head

  def removeMember(workspaceId: Long, memberRequest: MemberRoleRequest): OptionT[F, WorkspaceMemberEntry] =
    for {
      _ <- OptionT.some[F](
        logger.info(
          s"[REMOVING MEMBER] removing ${memberRequest.distinguishedName} from ${memberRequest.resource} in workspace ${memberRequest.resourceId}"
        )
      )

      registration <- OptionT(
        context.ldapRepository
          .findAll(memberRequest.resource, memberRequest.resourceId)
          .transact(context.transactor)
          .map(NonEmptyList.fromList)
      )

      _ <- OptionT.some[F](
        logger.info(
          s"[REMOVING MEMBER] found ${registration.size} ldap registration ${registration.map(_.commonName)} in db"
        )
      )

      member <- OptionT.liftF(
        registration
          .map(reg => context.memberRepository.find(workspaceId, reg.distinguishedName))
          .sequence
          .transact(context.transactor)
          .map(_.toList.flatten)
      )

      _ <- OptionT.some[F](
        logger.info(s"[REMOVING MEMBER] found the following members: ${member.map(_.distinguishedName).mkString(", ")}")
      )

      _ <- OptionT.liftF(
        registration
          .map(
            reg =>
              for {
                _ <- context.provisioningLDAPClient
                  .removeUserFromGroup(reg.distinguishedName, memberRequest.distinguishedName)
                  .value
                groupMembers <- context.provisioningLDAPClient.groupMembers(reg.distinguishedName)
                _ <- if (groupMembers.map(_.distinguishedName).contains(memberRequest.distinguishedName)) {
                  logger.error(
                    s"[REMOVING MEMBER] ${memberRequest.distinguishedName} wasn't properly removed from ${registration
                      .map(_.commonName)}"
                  )
                  throw new Exception()
                  ().pure[F]
                } else
                  logger
                    .info(
                      s"[REMOVING MEMBER] removed ${memberRequest.distinguishedName} from ${registration.map(_.commonName)}"
                    )
                    .pure[F]
              } yield ()
          )
          .sequence
      )

      _ <- OptionT.liftF(
        registration
          .map(reg => context.memberRepository.delete(reg.id.get, memberRequest.distinguishedName))
          .sequence
          .transact(context.transactor)
      )

      _ <- OptionT.some[F](logger.info(s"[REMOVING MEMBER] deleted ${memberRequest.distinguishedName} record"))

      result <- OptionT(convertRecord(member).map(_.headOption))
    } yield result

  override def availableMembers(filter: String): F[MemberSearchResult] =
    context.lookupLDAPClient.search(filter).onError {
      case e: Throwable =>
        logger.error(s"Failed to find members for filter $filter. ${e.getLocalizedMessage}", e).pure[F]
    }
}
