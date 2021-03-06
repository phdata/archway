package io.phdata.startup

import cats.effect.{ContextShift, Effect, Sync, Timer}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.clients.LDAPUser
import io.phdata.config.LDAPConfig
import io.phdata.models.LDAPRegistration
import io.phdata.repositories.Group

class ADGroupsSynchronizer[F[_]: Sync: Timer: ContextShift](context: AppContext[F])(
    implicit val F: Effect[F]
) extends ScheduledJob[F] with LazyLogging {

  val ldapConfig: LDAPConfig = context.appConfig.ldap

  private def ldapRegistrations: F[List[LDAPRegistration]] = {
    context.memberRepository.listLDAPRegistrations().transact(context.transactor)
  }

  private def membersWithGroup: F[Seq[Group]] = {
    context.memberRepository.groupMembers.transact(context.transactor)
  }

  override def work: F[Unit] = {
    if (context.appConfig.ldap.sync.getOrElse(true)) {
      for {
        _ <- logger.info("Synchronizing AD groups").pure[F]
        _ <- synchronize()
        _ <- logger.info("Synchronizing AD groups going to sleep for {}", ldapConfig.syncInterval).pure[F]
        _ <- Timer[F].sleep(ldapConfig.syncInterval)
        _ <- work
      } yield ()
    } else {
      logger.info("AD group synchronization is disabled").pure[F]
    }
  }

  def synchronize(): F[Unit] = {
    for {
      registrations <- ldapRegistrations
      members <- membersWithGroup
      _ <- registrations.traverse { lDAPRegistration =>
        for {
          lDAPUsers <- context.lookupLDAPClient.groupMembers(lDAPRegistration.distinguishedName)
          _ <- internalSync(lDAPRegistration, members, lDAPUsers)
        } yield ()
      }
    } yield ()
  }

  private def internalSync(
      lDAPRegistration: LDAPRegistration,
      members: Seq[Group],
      lDAPUsers: List[LDAPUser]
  ): F[Unit] = {

    val membersForLdapRegistration =
      members.find(_.groupDN == lDAPRegistration.distinguishedName).map(_.userDNs).getOrElse(List.empty)

    val dbAdd: F[List[Long]] = lDAPUsers.traverse { ldapUser =>
      if (!membersForLdapRegistration.contains(ldapUser.distinguishedName)) {
        logger.info(s"Adding member ${ldapUser.distinguishedName} to the database").pure[F] *>
          context.memberRepository
            .create(ldapUser.distinguishedName, lDAPRegistration.id.get)
            .transact(context.transactor) <*
          context.memberRepository
            .complete(lDAPRegistration.id.get, ldapUser.distinguishedName)
            .transact(context.transactor)
      } else {
        logger.debug(s"Member ${ldapUser.distinguishedName} already exists in the database")
        0L.pure[F]
      }
    }

    val dbRemove: F[List[Int]] = membersForLdapRegistration.traverse { member =>
      val notMemberOf = !lDAPUsers.map(_.distinguishedName).contains(member)

      if (notMemberOf) {
        logger.info(s"Removing member $member from the database").pure[F] *>
          context.memberRepository.delete(lDAPRegistration.id.get, member).transact(context.transactor)
      } else {
        logger.debug(s"Member $member exists in AD and the database").pure[F] *>
          0.pure[F]
      }
    }

    (dbRemove *> dbAdd).void
  }

}
