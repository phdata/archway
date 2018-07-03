package com.heimdali.repositories

import java.time.Instant

import cats.data.OptionT
import cats.effect.Sync
import com.heimdali.models.LDAPRegistration
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class LDAPRepositoryImpl
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
         l.created
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
        ++ fr"""
        inner join request_hive rh on rh.hive_database_id = h.id
        inner join workspace_request w on w.id = rh.workspace_request_id
        where w.id = $workspaceId and h.name = $databaseName
        """).query[LDAPRegistration].option
    )

}
