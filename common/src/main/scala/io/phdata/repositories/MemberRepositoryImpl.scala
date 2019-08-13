package io.phdata.repositories

import java.time.Instant

import io.phdata.repositories.syntax.SqlSyntax
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd
import io.phdata.models.DistinguishedName
import io.phdata.repositories.syntax.SqlSyntax

class MemberRepositoryImpl(sqlSyntax: SqlSyntax) extends MemberRepository {
  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def create(distinguishedName: String, ldapRegistrationId: Long): ConnectionIO[Long] =
    Statements.create(distinguishedName, ldapRegistrationId).withUniqueGeneratedKeys("id")

  override def complete(id: Long, distinguishedName: String): ConnectionIO[Int] =
    Statements.complete(id, distinguishedName).run

  override def get(id: Long): ConnectionIO[List[MemberRightsRecord]] =
    Statements.get(id).to[List]

  override def delete(ldapRegistrationId: Long, distinguishedName: String): doobie.ConnectionIO[Int] =
    Statements.remove(ldapRegistrationId, distinguishedName).run

  override def list(workspaceId: Long): doobie.ConnectionIO[List[MemberRightsRecord]] =
    Statements.list(workspaceId).to[List]

  override def find(
      workspaceRequestId: Long,
      distinguishedName: DistinguishedName
  ): doobie.ConnectionIO[List[MemberRightsRecord]] =
    Statements.find(workspaceRequestId, distinguishedName).to[List]

  object Statements {

    val listSelect: Fragment =
      sql"""
        select
            area,
            distinguished_name,
            name,
            resourceId,
            role
         from (
        select
          m.id as memberId,
          wd.workspace_request_id,
          'data' as area,
            m.distinguished_name,
            h.name,
            h.id as resourceId,
            'manager' as role
        from workspace_database wd
        inner join hive_database h on wd.hive_database_id = h.id
        inner join hive_grant hg on h.manager_group_id = hg.id
        inner join member m on m.ldap_registration_id = hg.ldap_registration_id

        union

        select
          m.id as memberId,
          wd.workspace_request_id,
          'data' as area,
            m.distinguished_name,
            h.name,
            h.id as resourceId,
            'readwrite' as role
        from workspace_database wd
        inner join hive_database h on wd.hive_database_id = h.id
        inner join hive_grant hg on h.readwrite_group_id = hg.id
        inner join member m on m.ldap_registration_id = hg.ldap_registration_id

        union

        select
          m.id as memberId,
          wd.workspace_request_id,
          'data' as area,
            m.distinguished_name,
            h.name,
            h.id as resourceId,
            'readonly' as role
        from workspace_database wd
        inner join hive_database h on wd.hive_database_id = h.id
        inner join hive_grant hg on h.readonly_group_id = hg.id
        inner join member m on m.ldap_registration_id = hg.ldap_registration_id

        union

        select
          m.id as memberId,
          wt.workspace_request_id,
          'topics' as area,
            m.distinguished_name,
            t.name,
            t.id as resourceId,
            'manager' as role
        from workspace_topic wt
        inner join kafka_topic t on wt.kafka_topic_id = t.id
        inner join topic_grant tg on t.manager_role_id = tg.id
        inner join member m on m.ldap_registration_id = tg.ldap_registration_id

        union

        select
          m.id as memberId,
          wt.workspace_request_id,
          'topics' as area,
            m.distinguished_name,
            t.name,
            t.id as resourceId,
            'readonly' as role
        from workspace_topic wt
        inner join kafka_topic t on wt.kafka_topic_id = t.id
        inner join topic_grant tg on t.readonly_role_id = tg.id
        inner join member m on m.ldap_registration_id = tg.ldap_registration_id

        union

        select
          m.id as memberId,
          wa.workspace_request_id,
          'applications' as area,
            m.distinguished_name,
            a.name,
            a.id as resourceId,
            'manager' as role
        from workspace_application wa
        inner join application a on wa.application_id = a.id
        inner join member m on m.ldap_registration_id = a.ldap_registration_id
        )
        """ ++ sqlSyntax.anonymousTable

    def list(workspaceRequestId: Long): Query0[MemberRightsRecord] =
      (listSelect ++ whereAnd(fr"workspace_request_id = $workspaceRequestId")).query[MemberRightsRecord]

    def create(distinguishedName: String, ldapRegistrationId: Long): Update0 =
      sql"""
          insert into member (distinguished_name, ldap_registration_id)
          values ($distinguishedName, $ldapRegistrationId)
       """.update

    def complete(ldapRegistrationId: Long, distinguishedName: String): Update0 =
      sql"update member set created = ${Instant.now()} where distinguished_name = $distinguishedName and ldap_registration_id = $ldapRegistrationId".update

    def remove(ldapRegistrationId: Long, distinguishedName: String): Update0 =
      sql"delete from member where ldap_registration_id = $ldapRegistrationId and distinguished_name = $distinguishedName".update

    def get(id: Long): Query0[MemberRightsRecord] =
      (listSelect ++ whereAnd(fr"memberId = $id")).query

    def find(workspaceRequestId: Long, distinguished_name: DistinguishedName): Query0[MemberRightsRecord] =
      (listSelect ++ whereAnd(
        fr"workspace_request_id = $workspaceRequestId",
        fr"distinguished_name = ${distinguished_name.value}"
      )).query

  }

}
