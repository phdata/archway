package com.heimdali.repositories

import com.heimdali.models.WorkspaceMember
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd
import java.time.Instant

class MemberRepositoryImpl extends MemberRepository {

  val baseSelect =
    sql"""
         select m.username, m.created
         from member m
         """

  def create(username: String, ldapRegistrationId: Long): ConnectionIO[Long] =
    sql"""
          insert into member (username, ldap_registration_id)
          values ($username, $ldapRegistrationId)
       """.update
      .withUniqueGeneratedKeys("id")

  def findByDatabase(
      databaseName: String,
      role: DatabaseRole
  ): ConnectionIO[List[WorkspaceMember]] =
    (baseSelect
      ++ sql"""
             inner join ldap_registration l on m.ldap_registration_id = l.id
             inner join hive_database h on
          """
      ++ Fragment.const(s"h.${role}_group_id")
      ++ fr""" = l.id where h.name = $databaseName""")
      .query[WorkspaceMember]
      .to[List]

  def complete(id: Long): ConnectionIO[Int] =
    sql"update member set created = ${Instant.now()} where id = $id".update.run

  def get(id: Long): ConnectionIO[WorkspaceMember] =
    (baseSelect ++ whereAnd(fr"m.id = $id")).query[WorkspaceMember].unique

}
