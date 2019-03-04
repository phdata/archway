package com.heimdali.repositories

import java.time.{Clock, Instant}

import com.heimdali.models.TopicGrant
import doobie._
import doobie.implicits._

trait TopicGrantRepository {

  def create(topicGrant: TopicGrant): ConnectionIO[Long]

  def topicAccess(id: Long): ConnectionIO[Int]

}

class TopicGrantRepositoryImpl(val clock: Clock)
  extends TopicGrantRepository {
  override def create(topicGrant: TopicGrant): ConnectionIO[Long] =
    Statements.create(topicGrant).withUniqueGeneratedKeys("id")

  override def topicAccess(id: Long): ConnectionIO[Int] =
    Statements.topicAccess(id).run

  object Statements {

    def create(topicGrant: TopicGrant): Update0 =
      sql"""
         insert into topic_grant (ldap_registration_id, actions)
         values (${topicGrant.ldapRegistration.id}, ${topicGrant.actions})
        """.update

    def topicAccess(id: Long): Update0 =
      sql"""
         update topic_grant
         set topic_access = ${Instant.now(clock)}
         where id = $id
        """.update

  }

}
