package com.heimdali.repositories

import java.time.Clock

import com.heimdali.models.Application
import doobie._
import doobie.implicits._

class ApplicationRepositoryImpl(val clock: Clock)
  extends ApplicationRepository {

  override def create(application: Application): doobie.ConnectionIO[Long] =
    Statements.insert(application).withUniqueGeneratedKeys("id")

  override def consumerGroupAccess(applicationId: Long): doobie.ConnectionIO[Int] =
    Statements.consumerGroupAccess(applicationId).run

  override def findByWorkspaceId(workspaceId: Long): doobie.ConnectionIO[List[Application]] =
    Statements
      .findByWorkspaceId(workspaceId)
      .to[List]
      .map(_.groupBy(h => (h._1, h._2)).map {
        case ((Statements.AppHeader(name, consumerGroup, id), ldap), group) =>
          Application(
            name,
            consumerGroup,
            fromRecord(ldap).copy(attributes = group.map(g => g._3.key -> g._3.value)),
            id)
      }.toList)

  object Statements {

    case class AppHeader(name: String, consumerGroup: String, id: Option[Long])

    type ApplicationRecord = (AppHeader, LDAPRecord, LDAPAttribute)

    def insert(application: Application): Update0 =
      sql"""
             insert into application (name, consumer_group_name, ldap_registration_id)
             values (${application.name}, ${application.consumerGroup}, ${application.group.id})
            """.update

    def consumerGroupAccess(applicationId: Long): Update0 =
      sql"""
           update application
           set consumer_group_access = ${clock.instant}
           where id = $applicationId
          """.update

    def findByWorkspaceId(workspaceId: Long): Query0[ApplicationRecord] =
      sql"""
           select
              a.name,
              a.consumer_group_name,
              a.id,

              l.distinguished_name,
              l.common_name,
              l.sentry_role,
              l.id,
              l.group_created,
              l.role_created,
              l.group_associated,

              la.key,
              la.value
           from application a
           inner join workspace_application wa on wa.application_id = a.id
           inner join ldap_registration l on a.ldap_registration_id = l.id
           inner join ldap_attribute la on la.ldap_registration_id = l.id

           where
              wa.workspace_request_id = $workspaceId
          """.query[ApplicationRecord]
  }

}
