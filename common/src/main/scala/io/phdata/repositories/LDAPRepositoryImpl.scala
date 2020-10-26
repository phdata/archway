package io.phdata.repositories

import java.time.Instant

import cats.data._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd
import io.phdata.models.LDAPRegistration

class LDAPRepositoryImpl extends LDAPRepository with LazyLogging {

  import LDAPRepositoryImpl.Statements._

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  def insertRecord(ldapRegistration: LDAPRegistration): ConnectionIO[Long] =
    for {
      _ <- logger.debug("{}", ldapRegistration.attributes).pure[ConnectionIO]
      id <- insert(ldapRegistration).withUniqueGeneratedKeys[Long]("id")
      _ <- logger.debug("{}", id).pure[ConnectionIO]
      _ <- ldapRegistration.attributes.map(a => insertAttribute(id, a._1, a._2).run).sequence
    } yield id

  override def create(ldapRegistration: LDAPRegistration): ConnectionIO[LDAPRegistration] =
    insertRecord(ldapRegistration).map(id => ldapRegistration.copy(id = Some(id)))

  override def delete(ldapRegistration: LDAPRegistration): ConnectionIO[Unit] =
    deleteRecord(ldapRegistration).run.void

  def reduce(data: List[LDAPRow]): List[LDAPRegistration] =
    data
      .groupBy(_._1)
      .map {
        case (ldap, group) =>
          fromRecord(ldap).copy(attributes = group.map(a => a._2.key -> a._2.value))
      }
      .toList

  def find(resource: String, resourceId: Long, role: String): OptionT[ConnectionIO, LDAPRegistration] =
    OptionT {
      (resource match {
        case "data" => LDAPRepositoryImpl.Statements.findData(resourceId, role)
      }).to[List].map(reduce(_).headOption)
    }

  def findAll(resource: String, resourceId: Long): ConnectionIO[List[LDAPRegistration]] =
    (resource match {
      case "data" => LDAPRepositoryImpl.Statements.findAllData(resourceId)
    }).to[List].map(reduce)

  override def groupCreated(id: Long, time: Instant): ConnectionIO[Int] =
    LDAPRepositoryImpl.Statements.groupCreated(id, time).run

  override def roleCreated(id: Long, time: Instant): ConnectionIO[Int] =
    LDAPRepositoryImpl.Statements.roleCreated(id, time).run

  override def groupAssociated(id: Long, time: Instant): ConnectionIO[Int] =
    LDAPRepositoryImpl.Statements.groupAssociated(id, time).run

}

object LDAPRepositoryImpl {

  object Statements {

    type LDAPRow = (LDAPRecord, LDAPAttribute)

    val select: Fragment =
      sql"""
       select
         l.distinguished_name,
         l.common_name,
         l.sentry_role,
         l.id,
         l.group_created,
         l.role_created,
         l.group_associated,

         la.attr_key,
         la.attr_value
       from
         ldap_registration l
       inner join ldap_attribute la on la.ldap_registration_id = l.id
      """

    def groupAssociated(id: Long, time: Instant): doobie.Update0 =
      sql"update ldap_registration set group_associated = $time where id = $id".update

    def roleCreated(id: Long, time: Instant): doobie.Update0 =
      sql"update ldap_registration set role_created = $time where id = $id".update

    def groupCreated(id: Long, time: Instant): doobie.Update0 =
      sql"update ldap_registration set group_created = $time where id = $id".update

    def findAllData(resourceId: Long): doobie.Query0[LDAPRow] =
      (select ++ fr"inner join hive_grant hg on hg.ldap_registration_id = l.id inner join hive_database h on hg.id IN (h.readonly_group_id, h.readwrite_group_id, h.manager_group_id)" ++ whereAnd(
            fr"h.id = $resourceId"
          )).query[LDAPRow]

    def findData(resourceId: Long, role: String): doobie.Query0[LDAPRow] =
      (select ++ fr"inner join hive_grant hg on hg.ldap_registration_id = l.id inner join hive_database h on " ++ Fragment
            .const(s"h.${role}_group_id") ++ fr" = hg.id" ++ whereAnd(fr"h.id = $resourceId")).query[LDAPRow]

    def insert(ldapRegistration: LDAPRegistration): Update0 =
      sql"""
       insert into ldap_registration (distinguished_name, common_name, sentry_role)
       values(
        ${ldapRegistration.distinguishedName.value},
        ${ldapRegistration.commonName},
        ${ldapRegistration.sentryRole}
       )
      """.update

    def deleteRecord(ldapRegistration: LDAPRegistration): Update0 =
      sql"""
       delete from ldap_registration where distinguished_name = ${ldapRegistration.distinguishedName.value}""".update

    def insertAttribute(ldapRegistrationId: Long, key: String, value: String): Update0 =
      sql"""
            insert into ldap_attribute (ldap_registration_id, attr_key, attr_value)
            values ($ldapRegistrationId, $key, $value)
        """.update

  }

}
