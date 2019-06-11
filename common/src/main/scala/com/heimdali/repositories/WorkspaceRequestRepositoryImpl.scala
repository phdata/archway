package com.heimdali.repositories

import java.time.Instant

import cats.data.OptionT
import cats.implicits._
import com.heimdali.models.{DatabaseRole, _}
import doobie._
import doobie.implicits._
import doobie.util.{Get, Read}
import doobie.util.fragments.{whereAnd, whereOr}

class WorkspaceRequestRepositoryImpl
  extends WorkspaceRequestRepository {

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def list(username: String): ConnectionIO[List[WorkspaceSearchResult]] =
    WorkspaceRequestRepositoryImpl.Statements.listQuery(username).to[List]

  override def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest] =
    OptionT(WorkspaceRequestRepositoryImpl.Statements.find(id).option)

  override def findUnprovisioned(): ConnectionIO[List[WorkspaceRequest]] =
    WorkspaceRequestRepositoryImpl.Statements.findUnprovisioned.to[List]

  override def markProvisioned(workspaceId: Long, time: Instant): ConnectionIO[Int] =
    WorkspaceRequestRepositoryImpl.Statements.markProvisioned(workspaceId, time).run

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

  override def findByUsername(distinguishedName: String): OptionT[doobie.ConnectionIO, WorkspaceRequest] =
    OptionT(WorkspaceRequestRepositoryImpl.Statements.findByUsername(distinguishedName).option)

  override def pendingQueue(role: ApproverRole): ConnectionIO[List[WorkspaceSearchResult]] =
    WorkspaceRequestRepositoryImpl.Statements.pending(role).to[List]

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
          wr.id,
          wr.name,
          wr.summary,
          wr.behavior,
          case
            when s.approvals = 2 then 'approved'
            else 'pending'
          end as status,
          c.phi_data,
          c.pci_data,
          c.pii_data,
          wr.request_date as requested,
          case
            when s.approvals = 2 then s.latestApproval
            else null
          end as fullyApproved,
          COALESCE(db.size, 0.0) as allocatedInGB,
          res.cores as maxCores,
          COALESCE(res.mem, 0.0) as maxMemoryInGB
        from workspace_request wr
        inner join compliance c on wr.compliance_id = c.id
        left join (select workspace_request_id, sum(case when "role" = 'risk' then 1 else 0 end) as risk_approved, sum(case when "role" = 'infra' then 1 else 0 end) as infra_approved, count(*) as approvals, max(approval_time) as latestApproval from approval group by workspace_request_id) as s on s.workspace_request_id = wr.id
        left join (select wd.workspace_request_id, sum(size_in_gb) as size from workspace_database as wd inner join hive_database as hd on wd.hive_database_id = hd.id group by wd.workspace_request_id) as db on db.workspace_request_id = wr.id
        left join (select wp.workspace_request_id, sum(max_cores) as cores, sum(max_memory_in_gb) as mem from workspace_pool wp inner join resource_pool rp on wp.resource_pool_id = rp.id group by wp.workspace_request_id) as res on res.workspace_request_id = wr.id
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

    def innerQueryWith(distinguishedName: String): Fragment =
      innerQuery ++ whereOr(fr"mrm.distinguished_name = $distinguishedName", fr"rrm.distinguished_name = $distinguishedName")

    def listQuery(distinguishedName: String): Query0[(WorkspaceSearchResult)] =
      (listFragment ++ fr"where wr.id in (" ++ innerQueryWith(distinguishedName) ++ fr") and wr.single_user = false").query

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

    def findUnprovisioned(): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.workspace_created is null")).query[WorkspaceRequest]

    def markProvisioned(id: Long, time: Instant): Update0 =
      sql"update workspace_request SET workspace_created = $time where id = $id".update

    def findByUsername(username: String): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.requested_by = $username", fr"wr.single_user = true")).query[WorkspaceRequest]

    def pending(role: ApproverRole): Query0[WorkspaceSearchResult] =
      (listFragment ++ whereAnd(fr"COALESCE(s." ++ Fragment.const(s"${role.show}_approved") ++ fr", 0) = 0", fr"wr.single_user = false")).query

  }

}
