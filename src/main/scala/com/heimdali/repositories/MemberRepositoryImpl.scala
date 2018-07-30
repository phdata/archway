package com.heimdali.repositories

import com.heimdali.models.WorkspaceMember
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd
import java.time.Instant

import cats.data.OptionT

class MemberRepositoryImpl extends MemberRepository {
  implicit val han = LogHandler.jdkLogHandler

  val baseSelect =
    sql"""
         select m.username, m.created, m.id
         from member m
         """

  override def create(username: String, ldapRegistrationId: Long): ConnectionIO[Long] =
    sql"""
          insert into member (username, ldap_registration_id)
          values ($username, $ldapRegistrationId)
       """.update
      .withUniqueGeneratedKeys("id")

  override def findByDatabase(
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

  override def complete(id: Long, username: String): ConnectionIO[Int] =
    sql"update member set created = ${Instant.now()} where username = $username and ldap_registration_id = $id".update.run

  override def get(id: Long): ConnectionIO[WorkspaceMember] =
    (baseSelect ++ whereAnd(fr"m.id = $id")).query[WorkspaceMember].unique

  override def delete(id: Long): doobie.ConnectionIO[Int] =
    sql"delete from member where id = $id".update.run

  override def find(registrationId: Long, username: String): OptionT[doobie.ConnectionIO, WorkspaceMember] =
    OptionT((baseSelect ++ whereAnd(fr"m.ldap_registration_id = $registrationId", fr"m.username = $username")).query[WorkspaceMember].option)
}
