package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models._
import doobie._
import doobie.implicits._
import doobie.util.fragments.{whereAnd, whereOr}

class WorkspaceRequestRepositoryImpl
  extends WorkspaceRequestRepository {

  override def list(username: String): ConnectionIO[List[WorkspaceRequest]] =
    WorkspaceRequestRepositoryImpl.Statements.listQuery(username).to[List].map(_.groupBy(_._1).map {
      case (req, groups) => req.copy(approvals = groups.flatMap(_._2))
    }.toList)

  override def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest] =
    OptionT(WorkspaceRequestRepositoryImpl.Statements.find(id).option)

  override def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long] =
    WorkspaceRequestRepositoryImpl.Statements.insert(workspaceRequest).withUniqueGeneratedKeys("id")

  override def linkHive(workspaceId: Long, hiveDatabaseId: Long): doobie.ConnectionIO[Int] =
    WorkspaceRequestRepositoryImpl.Statements.linkHive(workspaceId, hiveDatabaseId).run

  override def linkPool(workspaceId: Long, resourcePoolId: Long): doobie.ConnectionIO[Int] =
    WorkspaceRequestRepositoryImpl.Statements.linkPool(workspaceId, resourcePoolId).run

  override def linkTopic(workspaceId: Long, KafkaTopicId: Long): ConnectionIO[Int] =
    WorkspaceRequestRepositoryImpl.Statements.linkTopic(workspaceId, KafkaTopicId).run

  override def linkApplication(workspaceId: Long, applicationId: Long): doobie.ConnectionIO[Int] =
    WorkspaceRequestRepositoryImpl.Statements.linkApplication(workspaceId, applicationId).run

  override def findByUsername(username: String): OptionT[doobie.ConnectionIO, WorkspaceRequest] =
    OptionT(WorkspaceRequestRepositoryImpl.Statements.findByUsername(username).option)
}

object WorkspaceRequestRepositoryImpl {

  object Statements {
    def linkPool(workspaceId: Long, resourcePoolId: Long): Update0 =
      sql"""
        insert into workspace_pool (workspace_request_id, resource_pool_id)
        values ($workspaceId, $resourcePoolId)
        """.update

    def linkHive(workspaceId: Long, hiveDatabaseId: Long): Update0 =
      sql"""
        insert into workspace_database (workspace_request_id, hive_database_id)
        values ($workspaceId, $hiveDatabaseId)
        """.update

    def linkTopic(workspaceId: Long, kafkaTopicId: Long): Update0 =
      sql"""
         insert into workspace_topic (workspace_request_id, kafka_topic_id)
         values ($workspaceId, $kafkaTopicId)
        """.update

    def linkApplication(workspaceId: Long, applicationId: Long): Update0 =
      sql"""
         insert into workspace_application (workspace_request_id, application_id)
         values ($workspaceId, $applicationId)
        """.update

    val selectFragment: Fragment =
      fr"""
        select
          wr.name,
          wr.summary,
          wr.description,
          wr.behavior,
          wr.requested_by,
          wr.request_date,
          c.phi_data,
          c.pci_data,
          c.pii_data,
          c.id,
          wr.single_user,
          wr.id
        from workspace_request wr
        inner join compliance c on wr.compliance_id = c.id
        """

    val listFragment: Fragment =
      fr"""
        select
          wr.name,
          wr.summary,
          wr.description,
          wr.behavior,
          wr.requested_by,
          wr.request_date,
          c.phi_data,
          c.pci_data,
          c.pii_data,
          c.id,
          wr.single_user,
          wr.id,
          a.role,
          a.approver,
          a.approval_time,
          a.id
        from workspace_request wr
        inner join compliance c on wr.compliance_id = c.id
        left join approval a on a.workspace_request_id = wr.id
        """

    val innerQuery: Fragment =
      fr"""
        select wd.workspace_request_id
        from workspace_database wd
        inner join hive_database h on wd.hive_database_id = h.id
        inner join hive_grant mg on h.manager_group_id = mg.id
        inner join ldap_registration mr on mg.ldap_registration_id = mr.id
        inner join member mrm on mrm.ldap_registration_id = mr.id
        left join hive_grant rg on h.readonly_group_id = rg.id
        left join ldap_registration rr on rg.ldap_registration_id = rr.id
        left join member rrm on rrm.ldap_registration_id = mr.id
        """

    def innerQueryWith(username: String): Fragment =
      innerQuery ++ whereOr(fr"mrm.username = $username", fr"rrm.username = $username")

    def listQuery(username: String): Query0[(WorkspaceRequest, Option[Approval])] =
      (listFragment ++ fr"where wr.id in (" ++ innerQueryWith(username) ++ fr") and wr.single_user = false")
        .query[(WorkspaceRequest, Option[Approval])]

    def insert(workspaceRequest: WorkspaceRequest): Update0 =
      sql"""
          insert into workspace_request (
            name,
            summary,
            description,
            behavior,
            compliance_id,
            requested_by,
            request_date,
            single_user
          )
          values (
            ${workspaceRequest.name},
            ${workspaceRequest.summary},
            ${workspaceRequest.description},
            ${workspaceRequest.behavior},
            ${workspaceRequest.compliance.id},
            ${workspaceRequest.requestedBy},
            ${workspaceRequest.requestDate},
            ${workspaceRequest.singleUser}
          )
      """.update

    def find(id: Long): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.id = $id")).query[WorkspaceRequest]

    def findByUsername(username: String): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.requested_by = $username", fr"wr.single_user = true")).query[WorkspaceRequest]

  }

}
