package io.phdata.repositories

import java.time.Instant

import io.phdata.models.TopicGrant
import doobie.LogHandler
import doobie._
import doobie.implicits._

class TopicGrantRepositoryImpl extends TopicGrantRepository {

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  override def create(topicGrant: TopicGrant): ConnectionIO[Long] =
    Statements.create(topicGrant).withUniqueGeneratedKeys("id")

  override def topicAccess(id: Long, time: Instant): ConnectionIO[Int] =
    Statements.topicAccess(id, time).run

  object Statements {

    def create(topicGrant: TopicGrant): Update0 = {
      // ...
      sql"insert into topic_grant (ldap_registration_id, actions) values (${topicGrant.ldapRegistration.id}, ${topicGrant.actions})"
        .asInstanceOf[Fragment]
        .update
    }

    def topicAccess(id: Long, time: Instant): Update0 =
      sql"update topic_grant set topic_access = $time where id = $id".update

  }

}
