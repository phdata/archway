package com.heimdali.repositories

import java.time.{Clock, Instant}

import cats.data.OptionT
import com.heimdali.models.LDAPRegistration
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class LDAPRepositoryImpl(clock: Clock)
  extends LDAPRepository
    with LazyLogging {

  implicit val han = LogHandler.jdkLogHandler

  def updateCreated(id: Long): ConnectionIO[Int] =
    sql"""
      update ldap_registration
      set created = ${Instant.now()}
      where id = $id
      """.update.run

  override def complete(id: Long): ConnectionIO[LDAPRegistration] =
    for {
      _ <- updateCreated(id)
      result <- find(id).value
    } yield result.get

  def insertRecord(ldapRegistration: LDAPRegistration): ConnectionIO[Long] =
    sql"""
       insert into ldap_registration (distinguished_name, common_name, sentry_role)
       values(
        ${ldapRegistration.distinguishedName},
        ${ldapRegistration.commonName},
        ${ldapRegistration.sentryRole}
       )
      """.update.withUniqueGeneratedKeys[Long]("id")

  def find(id: Long): OptionT[ConnectionIO, LDAPRegistration] =
    OptionT((select ++ whereAnd(fr"id = $id")).query[LDAPRegistration].option)

  val select: Fragment =
    sql"""
       select
         l.distinguished_name,
         l.common_name,
         l.sentry_role,
         l.id,
         l.group_created,
         l.role_created,
         l.group_associated
       from
         ldap_registration l
      """

  override def create(ldapRegistration: LDAPRegistration): ConnectionIO[LDAPRegistration] =
    for {
      id <- insertRecord(ldapRegistration)
      result <- find(id).value
    } yield result.get

  override def find[A <: DatabaseRole](workspaceId: Long, databaseName: String, databaseRole: A): OptionT[ConnectionIO, LDAPRegistration] =
    OptionT(
      (
        select ++
          fr"inner join hive_database h on "
          ++ Fragment.const(s"h.${databaseRole.getClass.getSimpleName.toLowerCase().replace("$", "")}_group_id") ++ fr" = l.id"
          ++ whereAnd(fr"h.workspace_request_id = $workspaceId", fr"h.name = $databaseName")
        ).query[LDAPRegistration].option
    )

  override def groupCreated(id: Long): ConnectionIO[Int] =
    sql"update ldap_registration set group_created = ${Instant.now(clock)} where id = $id".update.run

  override def roleCreated(id: Long): ConnectionIO[Int] =
    sql"update ldap_registration set role_created = ${Instant.now(clock)} where id = $id".update.run

  override def groupAssociated(id: Long): ConnectionIO[Int] =
    sql"update ldap_registration set group_associated = ${Instant.now(clock)} where id = $id".update.run
}
