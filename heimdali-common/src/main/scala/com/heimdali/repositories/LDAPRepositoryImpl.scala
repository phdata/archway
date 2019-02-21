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

  def insertRecord(ldapRegistration: LDAPRegistration): ConnectionIO[Long] =
    LDAPRepositoryImpl.Statements
      .insert(ldapRegistration.distinguishedName, ldapRegistration.commonName, ldapRegistration.sentryRole)
      .withUniqueGeneratedKeys[Long]("id")

  def find(id: Long): OptionT[ConnectionIO, LDAPRegistration] =
    OptionT(LDAPRepositoryImpl.Statements.find(id).option)

  override def create(ldapRegistration: LDAPRegistration): ConnectionIO[LDAPRegistration] =
    for {
      id <- insertRecord(ldapRegistration)
      result <- find(id).value
    } yield result.get

  def find(resource: String, resourceId: Long, role: String): OptionT[ConnectionIO, LDAPRegistration] =
    OptionT {
      resource match {
        case "data" => LDAPRepositoryImpl.Statements.findData(resourceId, role).option
        case "topics" => LDAPRepositoryImpl.Statements.findTopics(resourceId, role).option
        case "applications" => LDAPRepositoryImpl.Statements.findApplications(resourceId, role).option
      }
    }

  def findAll(resource: String, resourceId: Long): ConnectionIO[List[LDAPRegistration]] =
    resource match {
      case "data" => LDAPRepositoryImpl.Statements.findAllData(resourceId).to[List]
      case "topics" => LDAPRepositoryImpl.Statements.findAllTopics(resourceId).to[List]
      case "applications" => LDAPRepositoryImpl.Statements.findAllApplications(resourceId).to[List]
    }

  override def groupCreated(id: Long): ConnectionIO[Int] =
    LDAPRepositoryImpl.Statements.groupCreated(id, Instant.now(clock)).run

  override def roleCreated(id: Long): ConnectionIO[Int] =
    LDAPRepositoryImpl.Statements.roleCreated(id, Instant.now(clock)).run

  override def groupAssociated(id: Long): ConnectionIO[Int] =
    LDAPRepositoryImpl.Statements.groupAssociated(id, Instant.now(clock)).run

}

object LDAPRepositoryImpl {

  object Statements {

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

    def find(id: Long): doobie.Query0[LDAPRegistration] =
      (select ++ whereAnd(fr"id = $id")).query[LDAPRegistration]

    def groupAssociated(id: Long, time: Instant): doobie.Update0 =
      sql"update ldap_registration set group_associated = $time where id = $id".update

    def roleCreated(id: Long, time: Instant): doobie.Update0 =
      sql"update ldap_registration set role_created = $time where id = $id".update

    def groupCreated(id: Long, time: Instant): doobie.Update0 =
      sql"update ldap_registration set group_created = $time where id = $id".update

    def findAllApplications(resourceId: Long): doobie.Query0[LDAPRegistration] =
      (select ++ fr"inner join application a on a.ldap_registration_id = l.id inner join workspace_application wa on wa.application_id = a.id" ++ whereAnd(fr"a.id = $resourceId"))
        .query[LDAPRegistration]

    def findAllTopics(resourceId: Long): doobie.Query0[LDAPRegistration] =
      (select ++ fr"inner join topic_grant tg on tg.ldap_registration_id = l.id inner join kafka_topic t on tg.id IN (t.readonly_role_id, t.manager_role_id)" ++ whereAnd(fr"t.id = $resourceId"))
        .query[LDAPRegistration]

    def findAllData(resourceId: Long): doobie.Query0[LDAPRegistration] =
      (select ++ fr"inner join hive_grant hg on hg.ldap_registration_id = l.id inner join hive_database h on hg.id IN (h.readonly_group_id, h.manager_group_id)" ++ whereAnd(fr"h.id = $resourceId"))
        .query[LDAPRegistration]

    def findApplications(resourceId: Long, role: String): doobie.Query0[LDAPRegistration] =
      (select ++ fr"inner join application a on a.ldap_registration_id = l.id inner join workspace_application wa on wa.application_id = a.id" ++ whereAnd(fr"a.id = $resourceId"))
        .query[LDAPRegistration]

    def findTopics(resourceId: Long, role: String): doobie.Query0[LDAPRegistration] =
      (select ++ fr"inner join topic_grant tg on tg.ldap_registration_id = l.id inner join kafka_topic t on " ++ Fragment.const(s"t.${role}_role_id") ++ fr" = tg.id" ++ whereAnd(fr"t.id = $resourceId"))
        .query[LDAPRegistration]

    def findData(resourceId: Long, role: String): doobie.Query0[LDAPRegistration] =
      (select ++ fr"inner join hive_grant hg on hg.ldap_registration_id = l.id inner join hive_database h on " ++ Fragment.const(s"h.${role}_group_id") ++ fr" = hg.id" ++ whereAnd(fr"h.id = $resourceId"))
        .query[LDAPRegistration]

    def insert(dn: String, cn: String, role: String): Update0 =
      sql"""
       insert into ldap_registration (distinguished_name, common_name, sentry_role)
       values(
        $dn,
        $cn,
        $role
       )
      """.update

    def statements(id: Long): Query0[LDAPRegistration] =
      (select ++ whereAnd(fr"id = $id")).query[LDAPRegistration]

  }

}
