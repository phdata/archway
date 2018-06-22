package com.heimdali.repositories

import java.time.Instant

import cats.data.OptionT
import cats.implicits._
import com.heimdali.models._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import doobie.util.fragments.{ whereAnd, whereAndOpt, in }

class WorkspaceRequestRepositoryImpl
  extends WorkspaceRequestRepository
    with LazyLogging {

  implicit val workspaceRequestComposite: Composite[WorkspaceRequest] =
    Composite[(String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])].imap(
      (t: (String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])) =>
        WorkspaceRequest(t._1, t._2, t._3, Compliance(t._4, t._5, t._6, t._7), t._8, t._9))(
      (w: WorkspaceRequest) => (w.name, w.requestedBy, w.requestDate, w.compliance.phiData, w.compliance.pciData, w.compliance.piiData, w.compliance.id, w.singleUser, w.id)
    )

  def insert(workspaceRequest: WorkspaceRequest): ConnectionIO[Long] =
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
      """.update.withUniqueGeneratedKeys("id")

  val innerQuery =
    fr"""
        select rh.workspace_request_id
        from request_hive rh
        inner join hive_database h on rh.hive_database_id = h.id
        inner join ldap_registration lr on h.managing_group_id = lr.id
        """

  def innerQueryWith(memberships: List[String]): Fragment =
    innerQuery ++ whereAnd(whereAndOpt(memberships.toNel.map(m => in(fr"system_name", m))))

  def listQuery(memberships: List[String]): Fragment =
    selectFragment ++ fr"where wr.id in (" ++ innerQueryWith(memberships) ++ fr")"

  override def list(memberships: List[String]): ConnectionIO[List[WorkspaceRequest]] =
    listQuery(memberships).query[WorkspaceRequest].to[List]

  val selectFragment: Fragment = fr"""
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

  def find(id: Long): OptionT[ConnectionIO, WorkspaceRequest] =
    OptionT((selectFragment ++ whereAnd(fr"wr.id = $id")).query[WorkspaceRequest].option)

  override def linkHive(id: Long, hiveId: Long): ConnectionIO[Int] =
    sql"insert into request_hive values ($id, $hiveId)".update.run

  override def linkYarn(id: Long, yarnId: Long): ConnectionIO[Int] =
    sql"insert into request_yarn values ($id, $yarnId)".update.run

  override def create(updatedWorkspace: WorkspaceRequest): ConnectionIO[WorkspaceRequest] =
    for {
      id <- insert(updatedWorkspace)
      result <- find(id).value
    } yield result.get
}