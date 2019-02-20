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

  def find(resource: String, resourceId: Long, role: String): OptionT[ConnectionIO, LDAPRegistration] =
    OptionT {
      resource match {
        case "data" =>
          (select ++ fr"inner join hive_grant hg on hg.ldap_registration_id = l.id inner join hive_database h on " ++ Fragment.const(s"h.${role}_group_id") ++ fr" = hg.id" ++ whereAnd(fr"h.id = $resourceId")).query[LDAPRegistration].option
        case "topics" =>
          (select ++ fr"inner join topic_grant tg on tg.ldap_registration_id = l.id inner join kafka_topic t on " ++ Fragment.const(s"t.${role}_role_id") ++ fr" = tg.id" ++ whereAnd(fr"t.id = $resourceId")).query[LDAPRegistration].option
        case "applications" =>
          (select ++ fr"inner join application a on a.ldap_registration_id = l.id inner join workspace_application wa on wa.application_id = a.id" ++ whereAnd(fr"a.id = $resourceId")).query[LDAPRegistration].option
      }
    }

  override def groupCreated(id: Long): ConnectionIO[Int] =
    sql"update ldap_registration set group_created = ${Instant.now(clock)} where id = $id".update.run

  override def roleCreated(id: Long): ConnectionIO[Int] =
    sql"update ldap_registration set role_created = ${Instant.now(clock)} where id = $id".update.run

  override def groupAssociated(id: Long): ConnectionIO[Int] =
    sql"update ldap_registration set group_associated = ${Instant.now(clock)} where id = $id".update.run
}
