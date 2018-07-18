package com.heimdali.repositories

import cats.data.OptionT
import com.heimdali.models._
import doobie._
import doobie.implicits._
import doobie.util.fragments.{whereAnd, whereOr}

class WorkspaceRequestRepositoryImpl
  extends WorkspaceRequestRepository {

  override def list(username: String): ConnectionIO[List[WorkspaceRequest]] =
    WorkspaceRequestRepositoryImpl.Statements.listQuery(username).to[List]

  override def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest] =
    OptionT(WorkspaceRequestRepositoryImpl.Statements.find(id).option)

  override def create(workspaceRequest: WorkspaceRequest): ConnectionIO[Long] =
    WorkspaceRequestRepositoryImpl.Statements.insert(workspaceRequest).withUniqueGeneratedKeys("id")
}

object WorkspaceRequestRepositoryImpl {

  object Statements {

    val selectFragment: Fragment =
      fr"""
                                  select
                                    wr.name,
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

    val innerQuery: Fragment =
      fr"""
        select h.workspace_request_id
        from hive_database h
        inner join ldap_registration mr on h.manager_group_id = mr.id
        inner join member mrm on mrm.ldap_registration_id = mr.id
        left join ldap_registration rr on h.readonly_group_id = rr.id
        left join member rrm on rrm.ldap_registration_id = mr.id
        """

    def innerQueryWith(username: String): Fragment =
      innerQuery ++ whereOr(fr"mrm.username = $username", fr"rrm.username = $username")

    def listQuery(username: String): Query0[WorkspaceRequest] =
      (selectFragment ++ fr"where wr.id in (" ++ innerQueryWith(username) ++ fr")")
        .query[WorkspaceRequest]

    def insert(workspaceRequest: WorkspaceRequest): Update0 =
      sql"""
          insert into workspace_request (
            name,
            compliance_id,
            requested_by,
            request_date,
            single_user
          )
          values (
            ${workspaceRequest.name},
            ${workspaceRequest.compliance.id},
            ${workspaceRequest.requestedBy},
            ${workspaceRequest.requestDate},
            ${workspaceRequest.singleUser}
          )
      """.update

    def find(id: Long): Query0[WorkspaceRequest] =
      (selectFragment ++ whereAnd(fr"wr.id = $id")).query[WorkspaceRequest]

  }

}