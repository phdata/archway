package com.heimdali.repositories

import java.time.Instant

import com.heimdali.models._
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class HiveAllocationRepositoryImpl extends HiveAllocationRepository {

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  def grant(role: DatabaseRole, hiveRole: Statements.HiveRole, ldap: LDAPRegistration, records: List[LDAPAttribute]): HiveGrant =
    HiveGrant(
      hiveRole.name,
      hiveRole.location,
      ldap.copy(attributes = records.map(a => a.key -> a.value).distinct),
      role,
      hiveRole.grantId,
      hiveRole.locationAccess,
      hiveRole.databaseAccess
    )

  private def convertHiveResult(items: List[Statements.HiveResult]): List[HiveAllocation] =
    items.groupBy(r => (r._1, r._2, r._3, r._5, r._6, r._8, r._9)).map {
      case ((header, manager, managerLDAP, Some(readwrite), Some(readwriteLDAP), Some(readonly), Some(readonlyLDAP)), group) =>
        HiveAllocation(
          header.name,
          header.location,
          header.size,
          None,
          grant(Manager, manager, managerLDAP, group.map(_._4)),
          Some(grant(ReadWrite, readwrite, readwriteLDAP, group.flatMap(_._7))),
          Some(grant(ReadOnly, readonly, readonlyLDAP, group.flatMap(_._10))),
          header.hiveId,
          header.directoryCreated,
          header.databaseCreated
        )
      case ((header, manager, managerLDAP, _, _, _, _), group) =>
        HiveAllocation(
          header.name,
          header.location,
          header.size,
          None,
          grant(Manager, manager, managerLDAP, group.map(_._4)),
          None,
          None,
          header.hiveId,
          header.directoryCreated,
          header.databaseCreated
        )
    }.toList

  override def find(id: Long): ConnectionIO[Option[HiveAllocation]] =
    Statements
    .find(id)
    .to[List]
    .map(r => convertHiveResult(r).headOption)

  override def create(hiveDatabase: HiveAllocation): ConnectionIO[Long] =
    Statements
      .insert(hiveDatabase)
      .withUniqueGeneratedKeys("id")

  override def findByWorkspace(id: Long): ConnectionIO[List[HiveAllocation]] =
    Statements
      .list(id)
      .to[List]
      .map(convertHiveResult)

  override def directoryCreated(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .directoryCreated(id, time)
      .run

  override def quotaSet(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .quotaSet(id, time)
      .run

  override def databaseCreated(id: Long, time: Instant): ConnectionIO[Int] =
    Statements
      .databaseCreated(id, time)
      .run

  object Statements {

    case class HiveRole(name: String,
                        location: String,
                        role: String,
                        grantId: Option[Long],
                        locationAccess: Option[Instant],
                        databaseAccess: Option[Instant])

    case class HiveHeader(name: String,
                          location: String,
                          size: Int,
                          hiveId: Option[Long],
                          directoryCreated: Option[Instant],
                          databaseCreated: Option[Instant])

    type HiveResult = (
      HiveHeader,
        HiveRole,
        LDAPRecord,
        LDAPAttribute,
        Option[HiveRole],
        Option[LDAPRecord],
        Option[LDAPAttribute],
        Option[HiveRole],
        Option[LDAPRecord],
        Option[LDAPAttribute]
      )

    val selectQuery: Fragment =
      sql"""
       select
         h.name,
         h.location,
         h.size_in_gb,
         h.id,
         h.directory_created,
         h.database_created,

         h.name,
         h.location,
         CAST('manager' as CHAR(7)),
         mg.id,
         mg.location_access,
         mg.database_access,

         m.distinguished_name,
         m.common_name,
         m.sentry_role,
         m.id,
         m.group_created,
         m.role_created,
         m.group_associated,

         ma.attr_key,
         ma.attr_value,

         h.name,
         h.location,
         CAST('readwrite' as CHAR(8)),
         rwg.id,
         rwg.location_access,
         rwg.database_access,

         rw.distinguished_name,
         rw.common_name,
         rw.sentry_role,
         rw.id,
         rw.group_created,
         rw.role_created,
         rw.group_associated,

         rwa.attr_key,
         rwa.attr_value,

         h.name,
         h.location,
         CAST('readonly' as CHAR(8)),
         rg.id,
         rg.location_access,
         rg.database_access,

         r.distinguished_name,
         r.common_name,
         r.sentry_role,
         r.id,
         r.group_created,
         r.role_created,
         r.group_associated,

         roa.attr_key,
         roa.attr_value
       from hive_database h
       inner join hive_grant mg on h.manager_group_id = mg.id
       inner join ldap_registration m on mg.ldap_registration_id = m.id
       inner join ldap_attribute ma on ma.ldap_registration_id = m.id
       left join hive_grant rwg on h.readwrite_group_id = rwg.id
       left join ldap_registration rw on rwg.ldap_registration_id = rw.id
       left join ldap_attribute rwa on rwa.ldap_registration_id = rw.id
       left join hive_grant rg on h.readonly_group_id = rg.id
       left join ldap_registration r on rg.ldap_registration_id = r.id
       left join ldap_attribute roa on roa.ldap_registration_id = r.id
      """

    def insert(hiveDatabase: HiveAllocation): Update0 =
      sql"""
         insert into hive_database (name, location, size_in_gb, manager_group_id, readwrite_group_id, readonly_group_id)
         values (${hiveDatabase.name}, ${hiveDatabase.location}, ${hiveDatabase.sizeInGB}, ${hiveDatabase.managingGroup.id}, ${hiveDatabase.readWriteGroup.flatMap(_.id)}, ${hiveDatabase.readonlyGroup.flatMap(_.id)})
         """.update

    def find(id: Long): Query0[HiveResult] =
      (selectQuery ++ whereAnd(fr"h.id = $id")).query[HiveResult]

    def list(workspaceId: Long): Query0[HiveResult] =
      (selectQuery ++ fr"inner join workspace_database wd on wd.hive_database_id = h.id" ++ whereAnd(fr"wd.workspace_request_id = $workspaceId")).query[HiveResult]

    def directoryCreated(id: Long, time: Instant): Update0 =
      sql"""
          update hive_database
          set directory_created = $time
          where id = $id
          """.update

    def quotaSet(id: Long, time: Instant): Update0 =
      sql"""
          update hive_database
          set quota_set = $time
          where id = $id
          """.update

    def databaseCreated(id: Long, time: Instant): Update0 =
      sql"""
          update hive_database
          set database_created = $time
          where id = $id
          """.update

  }

}
