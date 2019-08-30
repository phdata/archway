package io.phdata.repositories

import java.time.Instant

import cats.data.OptionT
import io.phdata.models._
import io.phdata.repositories.syntax.SqlSyntax
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class WorkspaceRequestRepositoryImpl(sqlSyntax: SqlSyntax) extends WorkspaceRequestRepository with LazyLogging {

  val statements = if (sqlSyntax.name == SqlSyntax.DEFAULT) {
    logger.debug("Chose default statements")
    new DefaultStatements
  } else {
    logger.debug("Chose oracle statements")
    new OracleStatements
  }

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def list(username: String): ConnectionIO[List[WorkspaceSearchResult]] =
    statements.listQuery(DistinguishedName(username)).to[List]

  override def userAccessible(userDn: DistinguishedName): ConnectionIO[List[Long]] =
    statements.userAccessibleQuery(userDn).to[List]

  override def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest] =
    OptionT(statements.find(id).option)

  override def findUnprovisioned(): ConnectionIO[List[WorkspaceRequest]] =
    statements.findUnprovisioned.to[List]

  override def markProvisioned(workspaceId: Long, time: Instant): ConnectionIO[Int] =
    statements.markProvisioned(workspaceId, time).run

  override def markUnprovisioned(workspaceId: Long): doobie.ConnectionIO[Int] =
    statements.markUnprovisioned(workspaceId).run

  override def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long] =
    statements.insert(workspaceRequest).withUniqueGeneratedKeys[Long]("id")

  override def linkHive(workspaceId: Long, hiveDatabaseId: Long): doobie.ConnectionIO[Int] =
    statements.linkHive(workspaceId, hiveDatabaseId).run

  override def linkPool(workspaceId: Long, resourcePoolId: Long): doobie.ConnectionIO[Int] =
    statements.linkPool(workspaceId, resourcePoolId).run

  override def linkTopic(workspaceId: Long, KafkaTopicId: Long): ConnectionIO[Int] =
    statements.linkTopic(workspaceId, KafkaTopicId).run

  override def linkApplication(workspaceId: Long, applicationId: Long): doobie.ConnectionIO[Int] =
    statements.linkApplication(workspaceId, applicationId).run

  override def findByUsername(distinguishedName: String): OptionT[doobie.ConnectionIO, WorkspaceRequest] =
    OptionT(statements.findByUsername(distinguishedName).option)

  override def pendingQueue(role: ApproverRole): ConnectionIO[List[WorkspaceSearchResult]] =
    statements.pending(role).to[List]

  override def deleteWorkspace(workspaceId: Long): doobie.ConnectionIO[Int] =
    statements.deleteWorkspace(workspaceId).run

  class DefaultStatements {

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
        left join (select workspace_request_id, sum(case when role = 'risk' then 1 else 0 end) as risk_approved, sum(case when role = 'infra' then 1 else 0 end) as infra_approved, count(*) as approvals, max(approval_time) as latestApproval from approval group by workspace_request_id) as s on s.workspace_request_id = wr.id
        left join (select wd.workspace_request_id, sum(size_in_gb) as size from workspace_database as wd inner join hive_database as hd on wd.hive_database_id = hd.id group by wd.workspace_request_id) as db on db.workspace_request_id = wr.id
        left join (select wp.workspace_request_id, sum(max_cores) as cores, sum(max_memory_in_gb) as mem from workspace_pool wp inner join resource_pool rp on wp.resource_pool_id = rp.id group by wp.workspace_request_id) as res on res.workspace_request_id = wr.id
        """

    def innerQuery(userDN: DistinguishedName): Fragment =
      fr"""
           select wd.workspace_request_id
           from workspace_database wd,
                hive_database h,
                ldap_registration mr,
                member mrm,
                hive_grant mg
           where mrm.distinguished_name = ${userDN.value}
             AND (
                   (h.readwrite_group_id = mg.id) OR
                   (h.readonly_group_id = mg.id) OR
                   (h.manager_group_id = mg.id))
             AND h.id = wd.hive_database_id
             AND mg.ldap_registration_id = mr.id
             AND mrm.ldap_registration_id = mr.id
        """

    def listQuery(userDN: DistinguishedName): Query0[(WorkspaceSearchResult)] =
      (listFragment ++ fr"where wr.id in (" ++ innerQuery(userDN) ++ fr") and wr.single_user = '0' and wr.deleted != '1'").query

    def userAccessibleQuery(userDN: DistinguishedName): doobie.Query0[(Long)] = innerQuery(userDN).query

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
            single_user,
            deleted
          )
          values (
            ${workspaceRequest.name},
            ${workspaceRequest.summary},
            ${workspaceRequest.description},
            ${workspaceRequest.behavior},
            ${workspaceRequest.compliance.id},
            ${workspaceRequest.requestedBy},
            ${workspaceRequest.requestDate},
            ${SqlSyntax.booleanToChar(workspaceRequest.singleUser).toString},
            '0'
          )
      """.update

    def deleteWorkspace(workspaceId: Long) =
      sql"""update workspace_request SET deleted = '1' where id = $workspaceId""".update

    def find(id: Long): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.id = $id")).query[WorkspaceRequest]

    def findUnprovisioned(): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.workspace_created is null")).query[WorkspaceRequest]

    def markProvisioned(id: Long, time: Instant): Update0 =
      sql"update workspace_request SET workspace_created = $time where id = $id".update

    def markUnprovisioned(id: Long): Update0 =
      sql"update workspace_request SET workspace_created = null where id = $id".update

    def findByUsername(username: String): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.requested_by = $username", fr"wr.single_user = '1'")).query[WorkspaceRequest]

    def pending(role: ApproverRole): Query0[WorkspaceSearchResult] =
      (listFragment ++ whereAnd(fr"wr.single_user = '0' AND wr.deleted != '1'")).query

  }

  class OracleStatements extends DefaultStatements {
    override val listFragment: Fragment =
      fr"""
        select wr.id,
               wr.name,
               wr.summary,
               wr.behavior,
               case
                   when s.approvals = 2 then 'approved'
                   else 'pending'
                   end                as status,
               c.phi_data,
               c.pci_data,
               c.pii_data,
               wr.request_date        as requested,
               case
                   when s.approvals = 2 then s.latestApproval
                   else null
                   end                as fullyApproved,
               COALESCE(db.total_size, 0.0) as allocatedInGB,
               res.cores              as maxCores,
               COALESCE(res.mem, 0.0) as maxMemoryInGB
        from workspace_request wr
                 inner join compliance c on wr.compliance_id = c.id
                 left join (select workspace_request_id,
                                   sum(case when role = 'risk' then 1 else 0 end)  as risk_approved,
                                   sum(case when role = 'infra' then 1 else 0 end) as infra_approved,
                                   count(*)                                          as approvals,
                                   max(approval_time)                                as latestApproval
                            from approval
                            group by workspace_request_id) s
        on s.workspace_request_id = wr.id
            left join (select wd.workspace_request_id, sum(size_in_gb) as total_size from workspace_database wd inner join hive_database hd on wd.hive_database_id = hd.id group by wd.workspace_request_id) db on db.workspace_request_id = wr.id
            left join (select wp.workspace_request_id, sum(max_cores) as cores, sum(max_memory_in_gb) as mem from workspace_pool wp inner join resource_pool rp on wp.resource_pool_id = rp.id group by wp.workspace_request_id) res on res.workspace_request_id = wr.id
        AND wr.single_user = '0'
        """

  }

}
