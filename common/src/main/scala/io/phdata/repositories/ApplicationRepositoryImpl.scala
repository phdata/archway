package io.phdata.repositories

import java.time.Instant

import io.phdata.models.Application
import doobie._
import doobie.implicits._

class ApplicationRepositoryImpl extends ApplicationRepository {

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  override def create(application: Application): doobie.ConnectionIO[Long] =
    Statements.insert(application).withUniqueGeneratedKeys("id")

  override def consumerGroupAccess(applicationId: Long, time: Instant): doobie.ConnectionIO[Int] =
    Statements.consumerGroupAccess(applicationId, time).run

  override def findByWorkspaceId(workspaceId: Long): doobie.ConnectionIO[List[Application]] =
    Statements
      .findByWorkspaceId(workspaceId)
      .to[List]
      .map(_.groupBy(h => (h._1, h._2)).map {
        case (
            (Statements.AppHeader(name, consumerGroup, applicationType, logo, language, repository, id), ldap),
            group
            ) =>
          Application(
            name,
            consumerGroup,
            fromRecord(ldap).copy(attributes = group.map(g => g._3.key -> g._3.value).distinct),
            applicationType,
            logo,
            language,
            repository,
            id
          )
      }.toList)

  object Statements {

    case class AppHeader(
        name: String,
        consumerGroup: String,
        applicationType: Option[String],
        logo: Option[String],
        language: Option[String],
        repository: Option[String],
        id: Option[Long]
    )

    type ApplicationRecord = (AppHeader, LDAPRecord, LDAPAttribute)

    def insert(application: Application): Update0 =
      sql"""
             insert into application (name, consumer_group_name, application_type, logo, language, repository, ldap_registration_id)
             values (${application.name}, ${application.consumerGroup}, ${application.applicationType}, ${application.logo}, ${application.language}, ${application.repository}, ${application.group.id})
            """.update

    def consumerGroupAccess(applicationId: Long, time: Instant): Update0 =
      sql"""
           update application
           set consumer_group_access = $time
           where id = $applicationId
          """.update

    def findByWorkspaceId(workspaceId: Long): Query0[ApplicationRecord] =
      sql"""
           select
              a.name,
              a.consumer_group_name,
              a.application_type,
              a.logo,
              a.language,
              a.repository,
              a.id,

              l.distinguished_name,
              l.common_name,
              l.sentry_role,
              l.id,
              l.group_created,
              l.role_created,
              l.group_associated,

              la.attr_key,
              la.attr_value
           from application a
           inner join workspace_application wa on wa.application_id = a.id
           inner join ldap_registration l on a.ldap_registration_id = l.id
           inner join ldap_attribute la on la.ldap_registration_id = l.id

           where
              wa.workspace_request_id = $workspaceId
          """.query[ApplicationRecord]
  }

}
