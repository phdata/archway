package com.heimdali.repositories

import java.time.Instant

import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class MemberRepositoryImpl extends MemberRepository {
  implicit val han = LogHandler.jdkLogHandler

  override def create(username: String, ldapRegistrationId: Long): ConnectionIO[Long] =
    Statements
      .create(username, ldapRegistrationId)
      .withUniqueGeneratedKeys("id")

  override def complete(id: Long, username: String): ConnectionIO[Int] =
    Statements
      .complete(id, username)
      .run

  override def get(id: Long): ConnectionIO[List[MemberRightsRecord]] =
    Statements
      .get(id)
      .to[List]

  override def delete(ldapRegistrationId: Long, username: String): doobie.ConnectionIO[Int] =
    Statements
      .remove(ldapRegistrationId, username)
      .run

  override def list(workspaceId: Long): doobie.ConnectionIO[List[MemberRightsRecord]] =
    Statements
      .list(workspaceId)
      .to[List]

  override def find(workspaceRequestId: Long, username: String): doobie.ConnectionIO[List[MemberRightsRecord]] =
    Statements
      .find(workspaceRequestId, username)
      .to[List]

  object Statements {

    val listSelect: Fragment =
      sql"""
        select
            area,
            username,
            name,
            resourceId,
            role
         from (
        select
          m.id as memberId,
          wd.workspace_request_id,
          'data' as area,
            m.username,
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
            m.username,
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
            m.username,
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
            m.username,
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
            m.username,
            a.name,
            a.id as resourceId,
            'manager' as role
        from workspace_application wa
        inner join application a on wa.application_id = a.id
        inner join member m on m.ldap_registration_id = a.ldap_registration_id
        ) as roles
        """

    def list(workspaceRequestId: Long): Query0[MemberRightsRecord] =
      (listSelect ++ whereAnd(fr"workspace_request_id = $workspaceRequestId")).query[MemberRightsRecord]

    def create(username: String, ldapRegistrationId: Long): Update0 =
      sql"""
          insert into member (username, ldap_registration_id)
          values ($username, $ldapRegistrationId)
       """.update

    def complete(ldapRegistrationId: Long, username: String): Update0 =
      sql"update member set created = ${Instant.now()} where username = $username and ldap_registration_id = $ldapRegistrationId".update

    def remove(ldapRegistrationId: Long, username: String): Update0 =
      sql"delete from member where ldap_registration_id = $ldapRegistrationId and username = $username".update

    def get(id: Long): Query0[MemberRightsRecord] =
      (listSelect ++ whereAnd(fr"memberId = $id")).query

    def find(workspaceRequestId: Long, username: String): Query0[MemberRightsRecord] =
      (listSelect ++ whereAnd(fr"workspace_request_id = $workspaceRequestId", fr"username = $username")).query

  }

}
