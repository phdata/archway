package io.phdata.repositories

import cats.data.OptionT
import io.phdata.models.Approval
import doobie._
import doobie.implicits._
import doobie.util.fragments.whereAnd

class ApprovalRepositoryImpl extends ApprovalRepository {
  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  def insert(id: Long, approval: Approval): ConnectionIO[Long] =
    sql"""
          insert into approval (role, approver, approval_time, workspace_request_id)
          values (${approval.role.toString.toLowerCase}, ${approval.approver}, ${approval.approvalTime}, $id)
      """.update.withUniqueGeneratedKeys[Long]("id")

  override def create(id: Long, approval: Approval): ConnectionIO[Approval] =
    for {
      id <- insert(id, approval)
      newApproval <- find(id).value
    } yield newApproval.get

  val selectQuery =
    sql"""
         select
           role,
           approver,
           approval_time,
           workspace_request_id
         from approval
      """

  override def find(id: Long): OptionT[ConnectionIO, Approval] =
    OptionT((selectQuery ++ whereAnd(fr"id = $id")).query[Approval].option)

  override def findByWorkspaceId(id: Long): ConnectionIO[List[Approval]] =
    (selectQuery ++ whereAnd(fr"workspace_request_id = $id")).query[Approval].to[List]
}
